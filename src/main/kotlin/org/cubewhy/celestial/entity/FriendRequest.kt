package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class FriendRequest(
    @Id
    val id: String,
    val senderId: String,
    val recipientId: String,
    val timestamp: Instant,
)