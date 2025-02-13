package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Message(
    @Id
    val id: String?,

    val senderId: String,
    val targetId: String,

    val message: String,

    val timestamp: Instant = Instant.now(),
)
