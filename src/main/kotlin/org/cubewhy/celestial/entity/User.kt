package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class User(
    @Id
    val id: String? = null,

    val username: String,
    val uuid: String,
    val role: Role,

    val radioPremium: Boolean = false,
    val lunarPlusColor: PlusColor? = null,
    val createdAt: Instant = Instant.now(),
    val lastSeenAt: Instant = Instant.now(),
    val allowFriendRequests: Boolean = true,
    val clothCloak: Boolean = true
)
