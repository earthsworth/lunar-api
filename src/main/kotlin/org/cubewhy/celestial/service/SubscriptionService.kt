package org.cubewhy.celestial.service

import com.lunarclient.websocket.subscription.v1.SubscribeRequest
import com.lunarclient.websocket.subscription.v1.SubscribeResponse
import com.lunarclient.websocket.subscription.v1.UnsubscribeRequest
import com.lunarclient.websocket.subscription.v1.UnsubscribeResponse
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface SubscriptionService : PacketProcessor {
    suspend fun processSubscribe(
        request: SubscribeRequest,
        session: WebSocketSession,
        user: User
    ): SubscribeResponse

    suspend fun getWorldPlayerUuids(session: WebSocketSession): List<String>
    suspend fun processUnsubscribe(
        request: UnsubscribeRequest,
        session: WebSocketSession,
        user: User
    ): UnsubscribeResponse
}