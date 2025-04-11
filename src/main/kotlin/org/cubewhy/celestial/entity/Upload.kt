package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Upload(
    @Id val id: String? = null,
    val sha256: String,

    val contentType: String
) : TrackingEntity()