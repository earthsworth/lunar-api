package org.cubewhy.celestial.entity.vo

data class SongVO(
    val id: String,
    val name: String,
    val thumbnail: String, // thumbnail upload id
    val songName: String,
    val artist: String,
    val album: String,
    val durationMillis: Int,

    val uploadId: String, // song upload id
    val createdAt: Long
)