package org.cubewhy.celestial.entity.vo

data class SongVO(
    val id: Long,
    val styngrId: String,
    val name: String,
    val image: String,
    val song: String,
    val artist: String,
    val album: String,
    val durationMillis: Int,
    val copyrightSafe: Boolean
)

