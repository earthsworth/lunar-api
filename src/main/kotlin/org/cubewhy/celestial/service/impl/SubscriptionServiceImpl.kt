package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.subscription.v1.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.event.UserSubscribeEvent
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class SubscriptionServiceImpl(
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
    ): WebsocketResponse {
        return when (method) {
            "Subscribe" -> this.processSubscribe(
                SubscribeRequest.parseFrom(payload),
                session,
                user
            ).toWebsocketResponse()
            "Unsubscribe" -> this.processUnsubscribe(
                UnsubscribeRequest.parseFrom(payload),
                session,
                user
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processUnsubscribe(
        request: UnsubscribeRequest,
        session: WebSocketSession,
        user: User
    ): UnsubscribeResponse {
        val uuids = request.targetUuidsList
        logger.info { "User ${user.username} update multiplayer player list (removed ${uuids.size} players)" }
        @Suppress("UNCHECKED_CAST")
        (session.attributes["multiplayer-uuids"] as MutableList<String>).removeAll(uuids.map { it.toUUIDString() }
            .toSet()) // set new uuid list
        return UnsubscribeResponse.getDefaultInstance()
    }

    override suspend fun processSubscribe(
        request: SubscribeRequest,
        session: WebSocketSession,
        user: User
    ): SubscribeResponse {
        val playerUuids = request.targetUuidsList.map { it.toUUIDString() }
        // save ids to session properties
        this.saveWorldPlayerUuids(session, playerUuids, user)

        return SubscribeResponse.newBuilder().build() // empty data
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
        // send event
        logger.debug { "Push UserJoinWorldEvent" }
        applicationEventPublisher.publishEvent(UserSubscribeEvent(this, user, uuids, session))
    }

    override fun getWorldPlayerUuids(session: WebSocketSession): List<String> {
        if (!session.attributes.containsKey("multiplayer-uuids")) {
            return emptyList()
        }
        @Suppress("UNCHECKED_CAST")
        return session.attributes["multiplayer-uuids"] as List<String>
    }
}