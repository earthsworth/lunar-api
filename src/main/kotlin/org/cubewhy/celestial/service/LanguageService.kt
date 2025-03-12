package org.cubewhy.celestial.service

import com.lunarclient.websocket.language.v1.UpdateLanguageRequest
import com.lunarclient.websocket.language.v1.UpdateLanguageResponse
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface LanguageService : PacketProcessor {
    suspend fun processUpdateLanguageRequest(
        request: UpdateLanguageRequest,
        session: WebSocketSession,
        user: User
    ): UpdateLanguageResponse
}