package org.cubewhy.celestial.entity

import com.fasterxml.jackson.annotation.JsonInclude
import org.cubewhy.celestial.entity.vo.BlogPostVO
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.cubewhy.celestial.entity.vo.StarServerVO

data class GameMetadataResponse(
    val blogPosts: List<BlogPostVO>,
    val alert: Alert,
    val modSettings: Map<String, ModSetting>,
    val clientSettings: Map<String, Map<String, Any?>>,
    val pinnedServers: List<PinnedServerVO>,
    val starServers: List<StarServerVO>,
    val featureFlag: List<FeatureFlag>,
    val serverIntegration: List<ServerIntegration>,
    val store: Map<String, Any>, // unknown
    val sentryFilteredExceptions: List<SentryFilter>,
    val langOverride: Map<String, Any>, // unknown
    val links: Links
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

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ModSetting(
    val enabled: Boolean? = null,
    val properties: Map<String, Any?>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
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
