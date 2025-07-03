package org.cubewhy.celestial.service

import com.lunarclient.websocket.language.v1.UpdateLanguageRequest
import com.lunarclient.websocket.language.v1.UpdateLanguageResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface LanguageService : PacketProcessor {
    suspend fun processUpdateLanguageRequest(
        request: UpdateLanguageRequest,
        connection: ClientConnection<*>,
        user: User
    ): UpdateLanguageResponse
}