package org.cubewhy.celestial.entity.dto

data class UpdatePasswordDTO(
    val oldPassword: String,
    val newPassword: String
)
