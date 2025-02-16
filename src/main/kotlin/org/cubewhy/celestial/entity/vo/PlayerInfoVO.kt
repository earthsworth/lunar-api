package org.cubewhy.celestial.entity.vo

data class PlayerInfoVO(
    val user: String,
    val online: Boolean,
    val mcName: String?,
    val mcUuid: String?,
    val roleColor: Int,
    val roleRank: String,
    val plus: Boolean
)
