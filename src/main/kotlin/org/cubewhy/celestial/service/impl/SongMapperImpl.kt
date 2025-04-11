package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.Song
import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.service.SongMapper
import org.springframework.stereotype.Service

@Service
class SongMapperImpl : SongMapper {
    override fun mapToLunarSongVO(song: Song, baseUrl: String): LunarSongVO {
        return LunarSongVO(
            id = song.numberId,
            styngrId = song.id!!,
            name = song.name,
            image = "${baseUrl}api/upload?id=${song.thumbnail}",
            song = song.songName,
            artist = song.artist,
            album = song.album,
            durationMillis = song.durationMillis,
            copyrightSafe = true
        )
    }

    override fun mapToSongVO(song: Song): SongVO {
        return SongVO(
            id = song.id!!,
            name = song.name,
            thumbnail = song.thumbnail,
            songName = song.songName,
            artist = song.artist,
            album = song.album,
            durationMillis = song.durationMillis,
            uploadId = song.uploadId,
            createdAt = song.createdAt.epochSecond
        )
    }
}