package org.cubewhy.celestial.entity.config

import org.cubewhy.celestial.entity.Role
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.UUID

@ConfigurationProperties(prefix = "lunar")
data class LunarProperties(
    var user: UserProperties,
    var discord: DiscordProperties,
    var upload: UploadProperties,
    var sentry: SentryProperties
) {
    data class UserProperties(
        var verify: Boolean,
        var roleAssignments: List<RoleAssignment>
    ) {
        data class RoleAssignment(
            val uuid: UUID,
            val roles: List<Role>
        )
    }

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