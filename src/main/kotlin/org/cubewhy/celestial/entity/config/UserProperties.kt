package org.cubewhy.celestial.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "lunar.user")
data class UserProperties(
    val verify: Boolean = false,
)
