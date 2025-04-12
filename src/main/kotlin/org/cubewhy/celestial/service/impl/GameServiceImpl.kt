package org.cubewhy.celestial.service.impl

import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.entity.vo.BlogPostVO
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.cubewhy.celestial.entity.vo.StarServerVO
import org.cubewhy.celestial.repository.BlogPostRepository
import org.cubewhy.celestial.repository.PinnedServerRepository
import org.cubewhy.celestial.service.BlogPostMapper
import org.cubewhy.celestial.service.GameService
import org.cubewhy.celestial.service.PinnedServerMapper
import org.cubewhy.celestial.util.extractBaseUrl
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange

@Service
class GameServiceImpl(
    private val lunarProperties: LunarProperties,
    private val blogPostRepository: BlogPostRepository,
    private val blogPostMapper: BlogPostMapper,
    private val pinnedServerRepository: PinnedServerRepository,
    private val pinnedServerMapper: PinnedServerMapper
) : GameService {
    override suspend fun metadata(branch: String, exchange: ServerWebExchange): GameMetadataResponse {
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
                FeatureFlag("BetaSentryReportHandler", false)
            ),
            sentryFilteredExceptions = lunarProperties.sentry.filters.map { SentryFilter(it.identifier, it.regex) },
            starServers = this.loadStarServers(),
            blogPosts = this.loadBlogPosts(exchange.extractBaseUrl()),
            alert = Alert(
                colors = mapOf(
                    "RED" to AlertColor("#59db4040", "#80db4040"),
                    "BLUE" to AlertColor("#592b71ce", "#802b71ce")
                ),
                active = AlertActive(
                    id = "729",
                    name = "Discord",
                    text = "Join our Discord for news, giveaways, and more!",
                    color = "RED",
                    icon = "BULLHORN",
                    dismissable = true,
                    link = "https://discord.lunarclient.top"
                )
            ),
            pinnedServers = this.loadPinnedServers()
        )
    }

    private suspend fun loadPinnedServers(): List<PinnedServerVO> {
        return pinnedServerRepository.findAll().map { pinnedServer ->
            pinnedServerMapper.mapToPinedServerVO(pinnedServer)
        }.collectList().awaitLast()
    }

    private suspend fun loadStarServers(): List<StarServerVO> {
        return pinnedServerRepository.findAll().mapNotNull { pinnedServer ->
            pinnedServerMapper.mapToStarServerVO(pinnedServer)
        }.map { it!! }.collectList().awaitLast()
    }

    private suspend fun loadBlogPosts(baseUrl: String): List<BlogPostVO> {
        return blogPostRepository.findAll().flatMap { blogPost ->
            mono { blogPostMapper.mapToBlogPostVO(blogPost, baseUrl) }
        }.collectList().awaitLast()
    }
}