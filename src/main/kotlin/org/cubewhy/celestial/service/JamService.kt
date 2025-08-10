package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.dto.CreateSongDTO
import org.cubewhy.celestial.entity.dto.ModifySongDTO
import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrSongVO
import org.cubewhy.celestial.protocol.ClientConnection
import org.springframework.security.core.Authentication

interface JamService : PacketProcessor {
    suspend fun availableSongs(baseUrl: String): List<LunarSongVO>
    suspend fun processLogin(connection: ClientConnection<*>, user: User): RpcResponse
    suspend fun styngrPlaySong(songId: String, baseUrl: String): StyngrSongVO
    suspend fun createSong(dto: CreateSongDTO, authentication: Authentication, baseUrl: String): SongVO
    suspend fun modifySong(dto: ModifySongDTO, authentication: Authentication, baseUrl: String): SongVO
    suspend fun listOwn(authentication: Authentication, baseUrl: String): List<SongVO>

    suspend fun purgeAll()
}