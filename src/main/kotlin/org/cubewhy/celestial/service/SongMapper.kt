package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.Song
import org.cubewhy.celestial.entity.vo.SongVO

interface SongMapper {
    fun mapToSongVO(song: Song): SongVO
}