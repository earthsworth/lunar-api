package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Analysis(
    @Id
    private val id: String? = null,

    private var userCount: Long,
    private var webUserCount: Long,
    private var onlineCount: Int,
    private val timestamp: Instant = Instant.now()
)
