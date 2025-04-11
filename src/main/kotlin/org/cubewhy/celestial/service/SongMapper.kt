package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.Song
import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.SongVO

interface SongMapper {
    fun mapToLunarSongVO(song: Song, baseUrl: String): LunarSongVO
    fun mapToSongVO(song: Song) : SongVO
}