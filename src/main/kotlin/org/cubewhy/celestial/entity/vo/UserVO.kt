package org.cubewhy.celestial.entity.vo

data class UserVO(
    val id: String,
    val username: String,
    val role: String,
    val gameUser: String? = null, // id
)