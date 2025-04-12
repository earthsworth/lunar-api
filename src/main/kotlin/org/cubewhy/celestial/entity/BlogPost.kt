package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class BlogPost(
    @Id val id: String? = null,
    val title: String,
    val image: String, // upload id
    val link: String
): TrackingEntity()
