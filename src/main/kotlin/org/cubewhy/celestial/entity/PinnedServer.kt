package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class PinnedServer(
    @Id val id: String? = null,
    var name: String,
    var address: String,
    var minecraftVersions: List<String>,
    var removable: Boolean,

    var starRegex: String?,

    val owner: String
) : TrackingEntity()
