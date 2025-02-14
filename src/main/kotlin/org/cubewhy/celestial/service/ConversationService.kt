package org.cubewhy.celestial.service

import com.lunarclient.websocket.chat.v1.WebsocketChatV1
import com.lunarclient.websocket.conversation.v1.WebsocketConversationV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface ConversationService : PacketProcessor {
    suspend fun processSendMessage(
        request: WebsocketConversationV1.SendConversationMessageRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketConversationV1.SendConversationMessageResponse
}