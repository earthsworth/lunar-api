package org.cubewhy.celestial.service.impl

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.entity.vo.BlogPostVO
import org.cubewhy.celestial.repository.BlogPostRepository
import org.cubewhy.celestial.repository.PinnedServerRepository
import org.cubewhy.celestial.service.BlogPostMapper
import org.cubewhy.celestial.service.GameService
import org.cubewhy.celestial.service.PinnedServerMapper
import org.springframework.stereotype.Service

@Service
class GameServiceImpl(
    private val lunarProperties: LunarProperties,
    private val blogPostRepository: BlogPostRepository,
    private val blogPostMapper: BlogPostMapper,
    private val pinnedServerRepository: PinnedServerRepository,
    private val pinnedServerMapper: PinnedServerMapper
) : GameService {

    override suspend fun metadata(branch: String, baseUrl: String): GameMetadataResponse {
        val pinnedServers = pinnedServerRepository.findRandomItems(3)
            .collectList().awaitFirstOrNull() ?: listOf()

        val activeAlert = if (lunarProperties.alert.show) {
            lunarProperties.alert.let { alert ->
                AlertActive(
                    id = "0721",
                    name = alert.name,
                    text = alert.text,
                    color = alert.color,
                    icon = alert.icon,
                    dismissable = alert.dismissable,
                    link = alert.link
                )
            }
        } else null

        return GameMetadataResponse(
            store = emptyMap(),
            langOverride = emptyMap(),
            links = Links(
                wrapped = "https://wrapped.lunarclient.com/access/",
                store = "https://lunarclient.top/",
                youtooz = "https://lunarclient.top/",
                trailer = "https://lunarclient.com/trailer"
            ),
            serverIntegration = emptyList(),
            modSettings = mapOf(
                "skyblockAddons" to ModSetting(
                    properties = mapOf(
                        "disableEmberRod" to false,
                        "hidePlayersNearNPC" to false,
                        "ignoreItemFrameClicks" to false,
                        "avoidPlacingEnchantItems" to false,
                        "avoidBreakingStems" to false,
                        "preventMovementOnDeath" to false,
                        "craftingPatterns" to false,
                        "craftingPattern" to false,
                        "jungleAxeCooldown" to false,
                        "onlyMineOresDeepCaverns" to false,
                        "onlyMineValuablesNether" to false,
                        "doubleWarp" to false,
                        "onlyBreakLogsPark" to false,
                        "dontOpenProfilesWithBow" to false
                    )
                )
            ),
            clientSettings = mapOf(
                "GENERAL" to emptyMap()
            ),
            featureFlag = listOf(
                FeatureFlag("Trailer", false),
                FeatureFlag("SentryIchorHandler", false),
                FeatureFlag("LunarPlus", true),
                FeatureFlag("DevSentryReportHandler", false),
                FeatureFlag("BetaSentryReportHandler", false),
                FeatureFlag("GamePromotionCTA", false),
                FeatureFlag("devTools", true)
            ),
            sentryFilteredExceptions = lunarProperties.sentry.filters.map { SentryFilter(it.identifier, it.regex) },
            starServers = pinnedServers.mapNotNull { pinnedServerMapper.mapToStarServerVO(it) },
            blogPosts = this.loadBlogPosts(baseUrl),
            alert = Alert(
                colors = mapOf(
                    "RED" to AlertColor("#59db4040", "#80db4040"),
                    "BLUE" to AlertColor("#592b71ce", "#802b71ce")
                ),
                active = activeAlert
            ),
            pinnedServers = pinnedServers.mapNotNull { pinnedServerMapper.mapToPinedServerVO(it) }
        )
    }

    private suspend fun loadBlogPosts(baseUrl: String): List<BlogPostVO> {
        return blogPostRepository.findAll().flatMap { blogPost ->
            mono { blogPostMapper.mapToBlogPostVO(blogPost, baseUrl) }
        }.collectList().awaitLast()
    }
}