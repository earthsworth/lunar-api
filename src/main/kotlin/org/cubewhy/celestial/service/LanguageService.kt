package org.cubewhy.celestial.service

import com.lunarclient.websocket.language.v1.WebsocketLanguageV1
import org.cubewhy.celestial.entity.User

interface LanguageService : PacketProcessor{
    fun processUpdateLanguageRequest(request: WebsocketLanguageV1.UpdateLanguageRequest, user: User)
}