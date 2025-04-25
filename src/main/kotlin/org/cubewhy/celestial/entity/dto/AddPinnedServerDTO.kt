package org.cubewhy.celestial.entity.dto

data class AddPinnedServerDTO(
    val name: String,
    val address: String,
    val minecraftVersions: List<String>,
)
