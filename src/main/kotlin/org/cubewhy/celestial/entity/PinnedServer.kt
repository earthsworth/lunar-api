package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class PinnedServer(
    @Id val id: String? = null,
    val name: String,
    val address: String,
    val minecraftVersions: List<String>,
    val removable: Boolean,

    val starRegex: String?
): TrackingEntity()
