package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.vo.SongVO
import org.springframework.web.reactive.socket.WebSocketSession

interface JamService : PacketProcessor {
    suspend fun availableSongs(): List<SongVO>
    suspend fun processLogin(session: WebSocketSession, user: User): WebsocketResponse
}