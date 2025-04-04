package org.cubewhy.celestial.entity.vo

import org.cubewhy.celestial.entity.Role

data class PlayerInfoVO(
    val user: String,
    val online: Boolean,
    val mcName: String?,
    val mcUuid: String?,
    val roleColor: Int,
    val roles: List<Role>,
    val plus: Boolean
)
