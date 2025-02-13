package org.cubewhy.celestial.service

import com.lunarclient.websocket.chat.v1.WebsocketChatV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface MessageV1Service : PacketProcessor {
    suspend fun processSendMessage(request: WebsocketChatV1.SendChatRequest ,user: User, session: WebSocketSession): WebsocketChatV1.SendChatResponse
}