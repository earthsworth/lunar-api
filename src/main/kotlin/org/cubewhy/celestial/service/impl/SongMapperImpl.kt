package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.Song
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.repository.UploadRepository
import org.cubewhy.celestial.service.SongMapper
import org.springframework.stereotype.Service

@Service
class SongMapperImpl : SongMapper {
    override fun mapToSongVO(song: Song, baseUrl: String): SongVO {
        return SongVO(
            id = song.numberId,
            styngrId = song.id!!,
            name = song.name,
            image = "${baseUrl}/api/upload?id=${song.image}",
            song = song.song,
            artist = song.artist,
            album = song.album,
            durationMillis = song.durationMillis,
            copyrightSafe = true
        )
    }
}