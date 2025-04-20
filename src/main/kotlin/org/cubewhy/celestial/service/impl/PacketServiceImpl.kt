package org.cubewhy.celestial.service.impl

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteString
import com.lunarclient.authenticator.v1.AuthSuccessMessage
import com.lunarclient.authenticator.v1.EncryptionRequestMessage
import com.lunarclient.common.v1.UuidAndUsername
import com.lunarclient.websocket.handshake.v1.Handshake
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.*
import org.cubewhy.celestial.util.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType
import java.security.KeyPair
import java.time.Instant
import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage as AuthenticatorServerboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage as AssetsServerboundWebSocketMessage

@Service
class PacketServiceImpl(
    private val userService: UserService,
    private val friendService: FriendService,
    private val sessionService: SessionService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val mojangService: MojangService,

    packetProcessors: List<PacketProcessor>,

    private val lunarProperties: LunarProperties
) : PacketService {

    private val packetProcessorMap = packetProcessors.associateBy { it.serviceName }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processAuthorize(
        session: WebSocketSession,
        message: AuthenticatorServerboundWebsocketMessage
    ): GeneratedMessage? {
        // load user
        if (message.hasHello()) {
            val hello = message.hello
            if (!lunarProperties.user.verify) {
                // verification is disabled
                // create or find user
                val user = userService.loadUser(hello.identity)
                // generate jwt
                val jwt = jwtUtil.createJwt(user)
                logger.debug { "User ${user.username} successfully authenticated" }
                return AuthSuccessMessage.newBuilder().apply {
                    this.jwt = jwt
                }.build()
            } else {
                // verify account
                // put user info into websocket session
                session.attributes["identity"] = hello.identity
                session.attributes["need-verify"] = true
                // generate public key
                val keyPair = CryptUtil.generateKeyPair()!!
                // save private key
                session.attributes["keypair"] = keyPair
                val pubKey = keyPair.public
                val randomBytes = generateRandomBytes()
                session.attributes["random-bytes"] = randomBytes
                // require the client to request Mojang API to verify
                return EncryptionRequestMessage.newBuilder().apply {
                    this.publicKey = pubKey.encoded.toByteString()
                    this.randomBytes = randomBytes.toByteString()
                }.build()
            }
        } else if (message.hasEncryptionFail()) {
            if ((session.attributes["need-verify"] as Boolean?) != true) {
                logger.warn { "Wrong packet order: hello is required" }
                // close socket
                session.close(CloseStatus.create(1008, "Bad packet order")).awaitFirstOrNull()
                return null
            }
            // get identity
            val identity = session.attributes["identity"] as UuidAndUsername

            val encryptionFail = message.encryptionFail
            logger.warn { "Failed to verify session with user ${identity.username} (${identity.uuid.toUUIDString()}): ${encryptionFail.reason}" }
        } else if (message.hasEncryptionResponse()) {
            val encryptionResponse = message.encryptionResponse
            // get server private key
            val keypair = session.attributes["keypair"] as KeyPair
            // decode random bytes
            val clientRandomBytes = CryptUtil.decryptData(keypair.private!!, encryptionResponse.publicKey.toByteArray())
            // verify random bytes
            val serverRandomBytes = session.attributes["random-bytes"] as ByteArray
            if (!clientRandomBytes.contentEquals(serverRandomBytes)) {
                // a middleman modded the connection, close the socket right now
                session.close(CloseStatus.create(1008, "Random bytes doesn't match")).awaitFirstOrNull()
                return null
            }
            // decode secret key
            val clientSecretKey =
                CryptUtil.decryptSharedKey(keypair.private!!, encryptionResponse.secretKey.toByteArray())
            // compute server hash
            val serverHash = CryptUtil.getServerIdHash("", keypair.public!!, clientSecretKey)
            // get identity
            val identity = session.attributes["identity"] as UuidAndUsername
            logger.debug { "Verifying player ${identity.username} with Mojang API" }
            // verify with Mojang API
            if (mojangService.hasJoined(identity.username, serverHash.toString())) {
                // create or find user
                val user = userService.loadUser(identity)
                // generate jwt
                val jwt = jwtUtil.createJwt(user)
                logger.debug { "User ${user.username} successfully authenticated (via Mojang API)" }
                return AuthSuccessMessage.newBuilder().apply {
                    this.jwt = jwt
                }.build()
            } else {
                logger.warn { "Invalid session: ${identity.username} (failed to verify with Mojang API)" }
                // failed to verify
                session.close(CloseStatus.create(1008, "Failed to verify session")).awaitFirstOrNull()
                return null
            }
        }
        return null // unknown packet
    }

    override suspend fun processHandshake(message: Handshake, session: WebSocketSession): User? {
        // this is the handshake packet (first packet)
        // parse packet
        val jwt = message.identity.authenticatorJwt
        // resolve JWT
        val decodedJWT = jwtUtil.resolveJwt(jwt)
        if (decodedJWT == null || Instant.now().isAfter(decodedJWT.expiresAtAsInstant)) {
            return null // bad jwt
        }
        val providedUUID = message.identity.player.uuid.toUUIDString()

        if (decodedJWT.claims["mcuuid"]?.asString() != providedUUID) {
            return null // uuid not match
        }
        val user = userService.loadUserByUuid(providedUUID)
        // valid token
        if (!decodedJWT.isValid(user.password) || jwtUtil.isInvalidToken(decodedJWT.id).awaitFirst()) {
            return null // jwt was revoked
        }
        // add to shared store
        sessionService.saveSession(user, session)
        // save game info to shared store
        if (message.hasGameHandshake()) {
            message.gameHandshake.let {
                sessionService.saveMinecraftVersion(user, it.minecraftVersion.enum)
                sessionService.saveLocation(user, it.location)
            }
        }
        logger.debug { "User ${user.username} logged in to the assets service" }
        return user
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User) {
        // remove user from shared store
        sessionService.removeSession(session)
        logger.debug { "User ${user.username} disconnected" }
        logger.debug { "Websocket terminated [${signalType.name}]" }
        if (!sessionService.isOnline(user)) {
            // save last seen timestamp
            userService.markOffline(user)
            // push event to friends
            friendService.userOffline(user)
        }
    }

    override suspend fun process(
        message: AssetsServerboundWebSocketMessage,
        session: WebSocketSession
    ): WebsocketResponse {
        val userId = session.attributes["user-id"] as String
        // find user
        val user = userRepository.findById(userId).awaitFirst()
        logger.debug { "User ${user.username} send packet ${message.service}:${message.method}" }

        val processor = packetProcessorMap[message.service]
        return processor?.process(message.method, message.input, session, user)
            ?: emptyWebsocketResponse()
    }
}