package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.dto.CreateSongDTO
import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrSongVO
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.server.ServerWebExchange

interface JamService : PacketProcessor {
    suspend fun availableSongs(exchange: ServerWebExchange): List<LunarSongVO>
    suspend fun processLogin(session: WebSocketSession, user: User): WebsocketResponse
    suspend fun styngrPlaySong(songId: String, baseUrl: String): StyngrSongVO
    suspend fun createSong(dto: CreateSongDTO, authentication: Authentication): SongVO
}