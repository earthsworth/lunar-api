package org.cubewhy.celestial.entity

data class GameMetadataResponse(
    val blogPosts: List<BlogPost>,
    val alert: Alert,
    val modSettings: Map<String, ModSetting>,
    val clientSettings: Map<String, Map<String, Any?>>,
    val pinnedServers: List<PinnedServer>,
    val starServers: List<StarServer>,
    val featureFlag: List<FeatureFlag>,
    val serverIntegration: List<ServerIntegration>,
    val store: Map<String, Any>, // unknown
    val sentryFilteredExceptions: List<SentryFilter>,
    val langOverride: Map<String, Any>, // unknown
    val links: Links
)

data class BlogPost(
    val title: String,
    val image: String,
    val link: String
)

data class Alert(
    val colors: Map<String, AlertColor>,
    val active: AlertActive?
)

data class AlertColor(
    val default: String,
    val hover: String
)

data class AlertActive(
    val id: String,
    val name: String,
    val text: String,
    val color: String,
    val icon: String,
    val dismissable: Boolean,
    val link: String? = null
)

data class ModSetting(
    val enabled: Boolean? = null,
    val properties: Map<String, Any?>? = null
)

data class PinnedServer(
    val name: String,
    val ip: String,
    val expirationDate: Long,
    val versions: List<String>,
    val removable: Boolean
)

data class StarServer(
    val pattern: String,
    val resource: String? = null
)

data class ServerIntegration(
    val ip: List<String>,
    val brand: String? = null,
    val modSettings: Map<String, ModSetting>? = null,
    val clientSettings: Map<String, Map<String, Any?>>? = null
)

//data class Store(
//    // Empty for now
//)
//
//data class LangOverride(
//    // Empty for now
//)

data class Links(
    val wrapped: String,
    val store: String,
    val youtooz: String,
    val trailer: String
)

data class SentryFilter(
    val identifier: String,
    val regex: String
)

data class FeatureFlag(
    val identifier: String,
    val value: Boolean
)
