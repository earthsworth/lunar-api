package org.cubewhy.celestial.entity.vo

data class PinnedServerVO(
    val id: String,

    val name: String,
    val ip: String,
    val expirationDate: Long = -1,
    val versions: List<String>,
    val removable: Boolean
)
