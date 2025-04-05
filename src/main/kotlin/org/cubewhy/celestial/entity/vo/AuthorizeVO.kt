package org.cubewhy.celestial.entity.vo

data class AuthorizeVO(
    val username: String,
    val token: String,
    val expire: Long,
    val roles: List<String>
)
