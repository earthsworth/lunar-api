package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.subscription.v1.SubscribeRequest
import com.lunarclient.websocket.subscription.v1.SubscribeResponse
import com.lunarclient.websocket.subscription.v1.UnsubscribeRequest
import com.lunarclient.websocket.subscription.v1.UnsubscribeResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.event.UserSubscribeEvent
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class SubscriptionServiceImpl(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val sessionService: SessionService
) : SubscriptionService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override val serviceName: String = "lunarclient.websocket.subscription.v1.SubscriptionService"

    override suspend fun process(
        method: String,
        payload: ByteString,
        connection: ClientConnection<*>,
        user: User
    ): RpcResponse {
        return when (method) {
            "Subscribe" -> this.processSubscribe(
                SubscribeRequest.parseFrom(payload),
                connection,
                user
            ).toWebsocketResponse()

            "Unsubscribe" -> this.processUnsubscribe(
                UnsubscribeRequest.parseFrom(payload),
                connection,
                user
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processUnsubscribe(
        request: UnsubscribeRequest,
        connection: ClientConnection<*>,
        user: User
    ): UnsubscribeResponse {
        val uuids = request.targetUuidsList
        logger.debug { "User ${user.username} update multiplayer player list (removed ${uuids.size} players)" }
        connection.metadata.multiplayerUuids.removeAll(uuids.map { it.toUUIDString() }
            .toSet()) // set new uuid list
        return UnsubscribeResponse.getDefaultInstance()
    }

    override suspend fun processSubscribe(
        request: SubscribeRequest,
        connection: ClientConnection<*>,
        user: User
    ): SubscribeResponse {
        val playerUuids = request.targetUuidsList.map { it.toUUIDString() }
        // save ids to session properties
        this.saveWorldPlayerUuids(connection, playerUuids, user)

        return SubscribeResponse.newBuilder().build() // empty data
    }

    fun saveWorldPlayerUuids(
        connection: ClientConnection<*>,
        uuids: List<String>,
        user: User
    ) {
        logger.debug { "User ${user.username} update multiplayer player list (added ${uuids.size} players)" }
        connection.metadata.multiplayerUuids.addAll(uuids)
        // send event
        logger.debug { "Push UserJoinWorldEvent" }
        applicationEventPublisher.publishEvent(UserSubscribeEvent(this, user, uuids, connection))
    }

    override suspend fun getWorldPlayerUuids(connection: ClientConnection<*>): List<String> {
        if (!connection.metadata.multiplayerUuids.isEmpty()) {
            return emptyList()
        }
        return connection.metadata.multiplayerUuids.filter { sessionService.isOnlineByUuid(it) }
    }
}