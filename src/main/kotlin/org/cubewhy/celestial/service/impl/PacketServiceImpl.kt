package org.cubewhy.celestial.service.impl

import com.google.protobuf.GeneratedMessage
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import com.lunarclient.websocket.handshake.v1.WebsocketHandshakeV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.CosmeticService
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.JwtUtil
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Instant

@Service
data class PacketServiceImpl(
    val userService: UserService,
    val cosmeticService: CosmeticService,
    val subscriptionService: SubscriptionService,
    val jwtUtil: JwtUtil
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
        logger.info { "User ${user.username} logged in to the assets service" }
        return user
    }

    override suspend fun process(
        message: WebsocketProtocolV1.ServerboundWebSocketMessage,
        session: WebSocketSession
    ): GeneratedMessage? {
        val user = session.attributes["user"] as User
        logger.info { "User ${user.username} send packet ${message.service}:${message.method}" }
        when (message.service) {
            "lunarclient.websocket.cosmetic.v1.CosmeticService" -> {
                return cosmeticService.process(message.method, message.input, session, user)
            }

            "lunarclient.websocket.subscription.v1.SubscriptionService" -> {
                return subscriptionService.process(message.method, message.input, session, user)
            }
        }
        return null
    }
}