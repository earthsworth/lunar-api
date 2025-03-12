package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.cosmetic.v1.UpdateCosmeticSettingsRequest
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface CosmeticService : PacketProcessor {
    suspend fun processLogin(user: User): GeneratedMessage
    suspend fun processUpdateCosmeticSettings(
        message: UpdateCosmeticSettingsRequest,
        user: User,
        session: WebSocketSession
    ): GeneratedMessage
}