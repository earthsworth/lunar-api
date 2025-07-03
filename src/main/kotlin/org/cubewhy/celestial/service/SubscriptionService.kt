package org.cubewhy.celestial.service

import com.lunarclient.websocket.subscription.v1.SubscribeRequest
import com.lunarclient.websocket.subscription.v1.SubscribeResponse
import com.lunarclient.websocket.subscription.v1.UnsubscribeRequest
import com.lunarclient.websocket.subscription.v1.UnsubscribeResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface SubscriptionService : PacketProcessor {
    suspend fun processSubscribe(
        request: SubscribeRequest,
        connection: ClientConnection<*>,
        user: User
    ): SubscribeResponse

    suspend fun getWorldPlayerUuids(connection: ClientConnection<*>): List<String>
    suspend fun processUnsubscribe(
        request: UnsubscribeRequest,
        connection: ClientConnection<*>,
        user: User
    ): UnsubscribeResponse
}