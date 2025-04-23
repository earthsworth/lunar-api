package org.cubewhy.celestial.service

import com.lunarclient.websocket.conversation.v1.SendConversationMessageRequest
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface ConversationService : PacketProcessor {
    suspend fun processSendMessage(
        request: SendConversationMessageRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun pushIrc(nickname: String, content: String, self: User? = null, fromDiscord: Boolean = false, force: Boolean = false)
    suspend fun muteUserInIrc(username: String)
    suspend fun unmuteUserInIrc(username: String)
}