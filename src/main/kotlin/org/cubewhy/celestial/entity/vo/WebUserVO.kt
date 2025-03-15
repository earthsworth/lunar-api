package org.cubewhy.celestial.entity.vo

data class WebUserVO(
    val id: String,
    val username: String,
    val role: String,
    val gameUser: String? = null,
)