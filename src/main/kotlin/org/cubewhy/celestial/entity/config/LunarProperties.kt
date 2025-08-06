package org.cubewhy.celestial.entity.config

import org.cubewhy.celestial.entity.Role
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

@ConfigurationProperties(prefix = "lunar")
data class LunarProperties(
    var upstream: UpstreamProperties,
    var user: UserProperties,
    var discord: DiscordProperties,
    var upload: UploadProperties,
    var sentry: SentryProperties,
    var alert: AlertProperties,
    var mojang: MojangProperties,
) {
    data class UpstreamProperties(
        var enabled: Boolean,
        var auth: String,
        var rpc: String,
    )

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

    data class AlertProperties(
        var show: Boolean,
        var name: String,
        var text: String,
        var color: String, // TODO: replace this with enum
        var icon: String, // TODO: replace this with enum
        var dismissable: Boolean,
        var link: String,
    )

    data class MojangProperties(
        var endpoints: Endpoints,
    ) {
        data class Endpoints(
            var hasJoined: String,
        )
    }
}