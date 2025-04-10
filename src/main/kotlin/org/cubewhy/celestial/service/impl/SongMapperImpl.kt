package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.Song
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.service.SongMapper
import org.springframework.stereotype.Service

@Service
class SongMapperImpl : SongMapper {
    override fun mapToSongVO(song: Song) = SongVO(
        id = song.id,
        styngrId = song.fakeStyngrId,
        name = song.name,
        image = song.image,
        song = song.song,
        artist = song.artist,
        album = song.album,
        durationMillis = song.durationMillis,
        copyrightSafe = true
    )
}