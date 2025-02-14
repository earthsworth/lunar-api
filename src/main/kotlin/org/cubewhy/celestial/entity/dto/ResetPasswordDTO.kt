package org.cubewhy.celestial.entity.dto

data class ResetPasswordDTO(
    val oldPassword: String,
    val password: String
)