package org.cubewhy.celestial.service.impl

import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import com.lunarclient.websocket.handshake.v1.WebsocketHandshakeV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.service.*
import org.cubewhy.celestial.util.JwtUtil
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType
import java.time.Instant

@Service
class PacketServiceImpl(
    private val userService: UserService,
    private val cosmeticService: CosmeticService,
    private val emoteService: EmoteService,
    private val friendService: FriendService,
    private val sessionService: SessionService,
    private val subscriptionService: SubscriptionService,
    private val languageService: LanguageService,
    private val conversationService: ConversationService,
    private val jwtUtil: JwtUtil,
) : PacketService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processAuthorize(message: LunarclientAuthenticatorV1.ServerboundWebSocketMessage): LunarclientAuthenticatorV1.AuthSuccessMessage? {
        // load user
        if (message.hasHello()) {
            val hello = message.hello
            val user = userService.loadUser(hello)
            // generate jwt
            val jwt = jwtUtil.createJwt(user)
            // create packet
            logger.info { "User ${user.username} successfully authenticated" }
            return LunarclientAuthenticatorV1.AuthSuccessMessage.newBuilder()
                .setJwt(jwt)
                .build()
        }
        return null // unknown packet
    }

    override suspend fun processHandshake(message: WebsocketHandshakeV1.Handshake, session: WebSocketSession): User? {
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
        // add to shared store
        sessionService.saveSession(user, session)
        // save game info to shared store
        if (message.hasGameHandshake()) {
            message.gameHandshake.let {
                sessionService.saveMinecraftVersion(user, it.minecraftVersion.enum)
                sessionService.saveLocation(user, it.location)
            }
        }
        logger.info { "User ${user.username} logged in to the assets service" }
        return user
    }

    override suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User) {
        // remove user from shared store
        sessionService.removeSession(user)
        logger.info { "User ${user.username} disconnected" }
        logger.info { "Websocket terminated [${signalType.name}]" }
        // save last seen timestamp
        userService.markOffline(user)
    }

    override suspend fun process(
        message: WebsocketProtocolV1.ServerboundWebSocketMessage,
        session: WebSocketSession
    ): WebsocketResponse {
        val user = session.attributes["user"] as User
        logger.info { "User ${user.username} send packet ${message.service}:${message.method}" }
        return when (message.service) {
            "lunarclient.websocket.cosmetic.v1.CosmeticService" -> cosmeticService.process(
                message.method,
                message.input,
                session,
                user
            )

            "lunarclient.websocket.emote.v1.EmoteService" -> emoteService.process(
                message.method,
                message.input,
                session,
                user
            )


            "lunarclient.websocket.subscription.v1.SubscriptionService" -> subscriptionService.process(
                message.method,
                message.input,
                session,
                user
            )


            "lunarclient.websocket.language.v1.LanguageService" -> languageService.process(
                message.method,
                message.input,
                session,
                user
            )


            "lunarclient.websocket.friend.v1.FriendService" -> friendService.process(
                message.method,
                message.input,
                session,
                user
            )

            "lunarclient.websocket.conversation.v1.ConversationService" -> conversationService.process(
                message.method,
                message.input,
                session,
                user
            )

            else -> emptyWebsocketResponse()
        }
    }
}