package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.cosmetic.v1.UpdateCosmeticSettingsRequest
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface CosmeticService : PacketProcessor {
    suspend fun processLogin(user: User): GeneratedMessage
    suspend fun processUpdateCosmeticSettings(
        message: UpdateCosmeticSettingsRequest,
        user: User,
        connection: ClientConnection<*>,
    ): GeneratedMessage
    suspend fun refreshCosmetics(user: User)
}