package org.cubewhy.celestial.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lunar")
data class LunarProperties(
    var user: UserProperties
) {
    data class UserProperties(
        var verify: Boolean
    )
}