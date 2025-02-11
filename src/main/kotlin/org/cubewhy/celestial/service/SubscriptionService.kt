package org.cubewhy.celestial.service

import com.lunarclient.websocket.subscription.v1.WebsocketSubscriptionV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface SubscriptionService : PacketProcessor {
    suspend fun processSubscribe(
        request: WebsocketSubscriptionV1.SubscribeRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketSubscriptionV1.SubscribeResponse

    fun getWorldPlayerUuids(session: WebSocketSession): List<String>
    suspend fun processUnsubscribe(
        request: WebsocketSubscriptionV1.UnsubscribeRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketSubscriptionV1.UnsubscribeResponse
}