package org.cubewhy.celestial.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lunar")
data class LunarProperties(
    var user: UserProperties,
    var discord: DiscordProperties
) {
    data class UserProperties(
        var verify: Boolean
    )

    data class DiscordProperties(
        var irc: DiscordIrcSyncProperties
    ) {
        data class DiscordIrcSyncProperties(
            var channel: Long
        )
    }
}