package org.cubewhy.celestial.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "lunar.sentry")
data class SentryProperties(
    val filters: List<SentryFilter> = emptyList()
)

data class SentryFilter(
    val identifier: String,
    val regex: String
)