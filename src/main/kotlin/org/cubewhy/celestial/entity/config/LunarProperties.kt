package org.cubewhy.celestial.entity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lunar")
data class LunarProperties(
    var user: UserProperties,
    var discord: DiscordProperties,
    var upload: UploadProperties,
    var sentry: SentryProperties
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

    data class UploadProperties(
        var maxSize: String
    )

    data class SentryProperties(
        var filters: List<SentryFilter> = emptyList()
    ) {
        data class SentryFilter(
            var identifier: String,
            var regex: String
        )
    }
}