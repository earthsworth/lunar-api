package org.cubewhy.celestial.service

import com.lunarclient.websocket.language.v1.WebsocketLanguageV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface LanguageService : PacketProcessor {
    suspend fun processUpdateLanguageRequest(
        request: WebsocketLanguageV1.UpdateLanguageRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketLanguageV1.UpdateLanguageResponse
}