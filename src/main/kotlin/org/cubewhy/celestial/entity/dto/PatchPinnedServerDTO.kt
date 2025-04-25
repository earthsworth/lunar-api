package org.cubewhy.celestial.entity.dto

data class PatchPinnedServerDTO(
    val name: String?,
    val address: String?,
    val minecraftVersions: List<String>?,
)