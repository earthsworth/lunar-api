package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Friend (
    @Id val id: String,
    val user1: String,
    val user2: String,
    val timestamp: Instant = Instant.now()
)