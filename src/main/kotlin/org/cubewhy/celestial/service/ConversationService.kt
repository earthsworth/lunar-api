package org.cubewhy.celestial.service

import com.lunarclient.websocket.conversation.v1.WebsocketConversationV1
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface ConversationService : PacketProcessor {
    suspend fun processSendMessage(
        request: WebsocketConversationV1.SendConversationMessageRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketResponse
}