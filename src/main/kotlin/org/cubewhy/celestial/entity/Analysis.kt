package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import java.time.Instant

data class Analysis(
    @Id
    private val id: String? = null,

    private var userCount: Long,
    private var webUserCount: Long,
    private var onlineCount: Int,
    private val timestamp: Instant = Instant.now()
)
