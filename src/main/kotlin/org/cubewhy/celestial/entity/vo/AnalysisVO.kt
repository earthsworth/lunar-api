package org.cubewhy.celestial.entity.vo

import java.io.Serializable
import java.time.Instant

data class AnalysisVO(
    var userCount: Long,
    var webUserCount: Long,
    var onlineCount: Long,
    val timestamp: Instant
): Serializable