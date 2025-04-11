package org.cubewhy.celestial.entity.dto

data class ModifySongDTO(
    val songId: String,
    val name: String,
    val thumbnail: String,
    val songName: String,
    val artist: String,
    val album: String,
    val durationMillis: Int,

    val uploadId: String
)
