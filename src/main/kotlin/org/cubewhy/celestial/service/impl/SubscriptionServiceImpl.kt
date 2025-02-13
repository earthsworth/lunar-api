package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.subscription.v1.WebsocketSubscriptionV1
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.event.UserJoinWorldEvent
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
data class SubscriptionServiceImpl(
    private val applicationEventPublisher: ApplicationEventPublisher
) : SubscriptionService {
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
        val uuids = request.targetUuidsList
        logger.info { "User ${user.username} update multiplayer player list (removed ${uuids.size} players)" }
        @Suppress("UNCHECKED_CAST")
        (session.attributes["multiplayer-uuids"] as MutableList<String>).removeAll(uuids.map { it.toUUIDString() }
            .toSet()) // set new uuid list
        return WebsocketSubscriptionV1.UnsubscribeResponse.getDefaultInstance()
    }

    override suspend fun processSubscribe(
        request: WebsocketSubscriptionV1.SubscribeRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketSubscriptionV1.SubscribeResponse {
        val playerUuids = request.targetUuidsList.map { it.toUUIDString() }
        // save ids to session properties
        this.saveWorldPlayerUuids(session, playerUuids, user)

        return WebsocketSubscriptionV1.SubscribeResponse.newBuilder().build() // empty data
    }

    fun saveWorldPlayerUuids(
        session: WebSocketSession,
        uuids: List<String>,
        user: User
    ) {
        logger.info { "User ${user.username} update multiplayer player list (added ${uuids.size} players)" }
        if (!session.attributes.containsKey("multiplayer-uuids")) {
            session.attributes["multiplayer-uuids"] = uuids.toMutableList()
        } else {
            @Suppress("UNCHECKED_CAST")
            (session.attributes["multiplayer-uuids"] as MutableList<String>).addAll(uuids)
        }
        if (uuids.size > 1) {
            // send event
            logger.info { "Push UserJoinWorldEvent" }
            applicationEventPublisher.publishEvent(UserJoinWorldEvent(this, user, uuids, session))
        }
    }

    override fun getWorldPlayerUuids(session: WebSocketSession): List<String> {
        if (!session.attributes.containsKey("multiplayer-uuids")) {
            return emptyList()
        }
        @Suppress("UNCHECKED_CAST")
        return session.attributes["multiplayer-uuids"] as List<String>
    }
}