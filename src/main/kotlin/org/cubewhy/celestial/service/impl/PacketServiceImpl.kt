package org.cubewhy.celestial.service.impl

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.kotlin.unpack
import com.lunarclient.authenticator.v1.AuthSuccessMessage
import com.lunarclient.authenticator.v1.EncryptionRequestMessage
import com.lunarclient.websocket.cosmetic.v1.PlayerCosmeticsPush
import com.lunarclient.websocket.handshake.v1.Handshake
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.UpstreamAuthResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.*
import org.cubewhy.celestial.util.*
import org.springframework.stereotype.Service
import reactor.core.publisher.SignalType
import java.time.Instant
import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage as AuthenticatorServerboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage as AssetsServerboundWebSocketMessage

@Service
class PacketServiceImpl(
    private val lunarProps: LunarProperties,
    private val userService: UserService,
    private val friendService: FriendService,
    private val sessionService: SessionService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val mojangService: MojangService,
    private val extendService: ExtendService,

    packetProcessors: List<PacketProcessor>,

    private val lunarProperties: LunarProperties
) : PacketService {

    private val packetProcessorMap = packetProcessors.associateBy { it.serviceName }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processAuthorize(
        connection: ClientConnection<*>,
        message: AuthenticatorServerboundWebsocketMessage,
        upstreamAuthenticatorConnection: ClientConnection<*>?,
    ): GeneratedMessage? {
        // load user
        if (message.hasHello()) {
            val hello = message.hello
            if (!lunarProperties.user.verify && upstreamAuthenticatorConnection == null) {
                // NOTE: if using extend service, the
                // verification is disabled
                // create or find user
                val user = userService.loadUser(hello.identity)
                // generate jwt
                val jwt = jwtUtil.createJwt(user)
                logger.info { "User ${user.username} successfully authenticated" }
                return AuthSuccessMessage.newBuilder().apply {
                    this.jwt = jwt
                }.build()
            } else if (upstreamAuthenticatorConnection != null) {
                // store identity
                connection.metadata.identity = message.hello.identity
                // Auth with upstream authenticator
                logger.info { "Auth with upstream authenticator for player ${message.hello.identity.username}" }
                // await for EncryptionRequestMessage
                val authResponse: UpstreamAuthResponse = try {
                    extendService.awaitForAuthResponse(upstreamAuthenticatorConnection) {
                        upstreamAuthenticatorConnection.send(message) // send auth message
                    }
                        ?: throw NullPointerException("Upstream responded null")
                } catch (e: Exception) {
                    logger.error(e) { "Failed to auth with upstream (handshake step)" }
                    // close the connection
                    connection.close(1001, "Failed to auth with upstream (handshake step)")
                    return null
                }

                if (authResponse.authSuccessMessage != null) {
                    // The upstream doesn't require auth
                    // find or create user
                    val user = userService.loadUser(message.hello.identity)
                    // get jwt from authResponse
                    val upstreamToken = authResponse.authSuccessMessage.jwt
                    val token = jwtUtil.createJwt(user, upstreamToken)
                    return AuthSuccessMessage.newBuilder().apply {
                        this.jwt = token
                    }.build()
                } else if (authResponse.encryptRequest != null) {
                    // let client process join server request
                    return authResponse.encryptRequest
                }

            } else {
                // verify account
                // put user info into websocket session
                connection.metadata.identity = hello.identity
                connection.metadata.needVerify = true
                // generate public key
                val keyPair = CryptUtil.generateKeyPair()!!
                // save private key
                connection.metadata.keypair = keyPair
                val pubKey = keyPair.public
                val randomBytes = generateRandomBytes()
                connection.metadata.randomBytes = randomBytes
                // require the client to request Mojang API to verify
                return EncryptionRequestMessage.newBuilder().apply {
                    this.publicKey = pubKey.encoded.toByteString()
                    this.randomBytes = randomBytes.toByteString()
                }.build()
            }
        } else if (message.hasEncryptionFail()) {
            if (connection.metadata.needVerify) {
                logger.warn { "Wrong packet order: hello is required" }
                // close socket
                connection.close(1008, "Bad packet order")
                return null
            }
            // get identity
            val identity = connection.metadata.identity!!

            val encryptionFail = message.encryptionFail
            logger.warn { "Failed to verify session with user ${identity.username} (${identity.uuid.toUUIDString()}): ${encryptionFail.reason}" }
        } else if (message.hasEncryptionResponse()) {
            if (upstreamAuthenticatorConnection != null) {
                if (!(upstreamAuthenticatorConnection.isOpen)) {
                    connection.close(1001, "Upstream authenticator closed")
                    return null
                }
                // wait for auth success message
                val authResponse: UpstreamAuthResponse = try {
                    extendService.awaitForAuthResponse(upstreamAuthenticatorConnection) {
                        // send back auth response
                        // let upstream request hasJoined api
                        upstreamAuthenticatorConnection.send(message)
                    }
                        ?: throw NullPointerException("Upstream responded null")
                } catch (e: Exception) {
                    logger.error(e) { "Failed to auth with upstream (join server step)" }
                    // close the connection
                    connection.close(1001, "Failed to auth with upstream (join server step)")
                    return null
                }
                if (authResponse.authSuccessMessage == null) {
                    // unreachable in most cases
                    logger.error { "Upstream handle encrypt request unexpectedly (null authSuccess message)" }
                    connection.close(1001, "Upstream handle encrypt request unexpectedly (null authSuccess message)")
                    return null // manual return null
                }
                // build jwt token
                // find or create user
                val user = userService.loadUser(connection.metadata.identity!!)
                // get jwt from authResponse
                val upstreamToken = authResponse.authSuccessMessage.jwt
                logger.info { "User ${user.username} completed auth with upstream" }
                val token = jwtUtil.createJwt(user, upstreamToken)
                return AuthSuccessMessage.newBuilder().apply {
                    this.jwt = token
                }.build()
            }
            val encryptionResponse = message.encryptionResponse
            // get server private key
            val keypair = connection.metadata.keypair!!
            // decode random bytes
            val clientRandomBytes = CryptUtil.decryptData(keypair.private!!, encryptionResponse.publicKey.toByteArray())
            // verify random bytes
            val serverRandomBytes = connection.metadata.randomBytes!!
            if (!clientRandomBytes.contentEquals(serverRandomBytes)) {
                // a middleman modded the connection, close the socket right now
                connection.close(1008, "Random bytes doesn't match")
                return null
            }
            // decode secret key
            val clientSecretKey =
                CryptUtil.decryptSharedKey(keypair.private!!, encryptionResponse.secretKey.toByteArray())
            // compute server hash
            val serverHash = CryptUtil.getServerIdHash("", keypair.public!!, clientSecretKey)
            // get identity
            val identity = connection.metadata.identity!!
            logger.info { "Verifying player ${identity.username} with Mojang API" }
            // verify with Mojang API
            if (mojangService.hasJoined(identity.username, serverHash.toString())) {
                // create or find user
                val user = userService.loadUser(identity)
                // generate jwt
                val jwt = jwtUtil.createJwt(user)
                logger.info { "User ${user.username} successfully authenticated (via Mojang API)" }
                return AuthSuccessMessage.newBuilder().apply {
                    this.jwt = jwt
                }.build()
            } else {
                logger.warn { "Invalid session: ${identity.username} (failed to verify with Mojang API)" }
                // failed to verify
                connection.close(1008, "Failed to verify session")
                return null
            }
        }
        return null // unknown packet
    }

    override suspend fun processHandshake(message: Handshake, connection: ClientConnection<*>): User? {
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
        sessionService.saveSession(user, connection)
        // save game info to shared store
        if (message.hasGameHandshake()) {
            message.gameHandshake.let {
                sessionService.saveMinecraftVersion(user, it.minecraftVersion.enum)
                sessionService.saveLocation(user, it.location)
            }
        }
        logger.debug { "User ${user.username} logged in to the assets service" }
        val upstreamToken = decodedJWT.claims["upstream-token"]
        if (upstreamToken?.isNull == false) {
            // connect to upstream
            val upstreamRpcConnection = extendService.openRpcConnection(message, upstreamToken.asString()) { message ->
                // TODO: move to another service
                if (message.hasPushNotification()) {
                    if (message.pushNotification.typeUrl == "type.googleapis.com/lunarclient.websocket.cosmetic.v1.PlayerCosmeticsPush") {
                        // forward cosmetics push
                        // forward to client
                        // parse push
                        val push = message.pushNotification.unpack<PlayerCosmeticsPush>()
                        if (!sessionService.isOnlineByUuid(push.playerUuid.toUUIDString())) {
                            // only forward if user not online (using the official lunarclient rpc service)
                            connection.send(message)
                        }
                    }
                }
            }
            connection.metadata.upstreamConnection = upstreamRpcConnection
        }
        return user
    }

    override suspend fun processDisconnect(signalType: SignalType, connection: ClientConnection<*>, user: User) {
        // remove user from shared store
        sessionService.removeSession(connection)
        logger.info { "User ${user.username} disconnected" }
        logger.debug { "Websocket terminated [${signalType.name}]" }
        // close upstream connection
        connection.metadata.upstreamConnection?.close(1000, "Completed")
        if (!sessionService.isOnline(user)) {
            // save last seen timestamp
            userService.markOffline(user)
            // push event to friends
            friendService.userOffline(user)
        }
    }

    override suspend fun process(
        message: AssetsServerboundWebSocketMessage,
        connection: ClientConnection<*>
    ): RpcResponse {
        val userId = connection.metadata.userId!!
        // find user
        val user = userRepository.findById(userId).awaitFirst()
        logger.info { "User ${user.username} send packet ${message.service}:${message.method}" }

        val processor = packetProcessorMap[message.service]
        return processor?.process(message.method, message.input, connection, user)
            ?: emptyWebsocketResponse()
    }
}