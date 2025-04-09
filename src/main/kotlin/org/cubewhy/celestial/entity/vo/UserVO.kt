package org.cubewhy.celestial.entity.vo

data class UserVO(
    val id: String,

    val username: String,
    val uuid: String,
    val roles: List<String> = listOf(),
)
