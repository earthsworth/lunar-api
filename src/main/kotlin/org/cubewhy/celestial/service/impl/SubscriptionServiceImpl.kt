package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.subscription.v1.WebsocketSubscriptionV1
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class SubscriptionServiceImpl : SubscriptionService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        return when (method) {
            "Subscribe" -> this.processSubscribe(
                WebsocketSubscriptionV1.SubscribeRequest.parseFrom(payload),
                session,
                user
            )

            "Unsubscribe" -> this.processUnsubscribe(
                WebsocketSubscriptionV1.UnsubscribeRequest.parseFrom(payload),
                session,
                user
            )

            else -> null
        }
    }

    override suspend fun processUnsubscribe(
        request: WebsocketSubscriptionV1.UnsubscribeRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketSubscriptionV1.UnsubscribeResponse {
        // remove all uuids from session
        logger.info { "User ${user.username} unsubscribed" }
        session.attributes["multiplayer-uuids"] = emptyList<String>() // clear uuid list
        return WebsocketSubscriptionV1.UnsubscribeResponse.getDefaultInstance()
    }

    override suspend fun processSubscribe(
        request: WebsocketSubscriptionV1.SubscribeRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketSubscriptionV1.SubscribeResponse {
        val playerUuids = request.targetUuidsList.map { it.toUUIDString() }
        // save ids to redis
        this.saveWorldPlayerUuids(session, playerUuids, user)

        return WebsocketSubscriptionV1.SubscribeResponse.newBuilder().build() // empty data
    }

    fun saveWorldPlayerUuids(
        session: WebSocketSession,
        uuids: List<String>,
        user: User
    ) {
        logger.info { "User ${user.username} update multiplayer player list (${uuids.size} players)" }
        session.attributes["multiplayer-uuids"] = uuids
    }

    override fun getWorldPlayerUuids(session: WebSocketSession): List<String> {
        if (!session.attributes.containsKey("multiplayer-uuids")) {
            return emptyList()
        }
        @Suppress("UNCHECKED_CAST")
        return session.attributes["multiplayer-uuids"] as List<String>
    }
}