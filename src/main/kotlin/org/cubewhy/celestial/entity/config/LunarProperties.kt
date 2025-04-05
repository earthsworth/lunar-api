package org.cubewhy.celestial.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lunar")
data class LunarProperties(
    val user: UserProperties
) {
    data class UserProperties(
        val verify: Boolean
    )
}