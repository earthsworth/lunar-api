package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.service.GameService
import org.springframework.stereotype.Service

@Service
class GameServiceImpl : GameService {
    override suspend fun metadata(branch: String): GameMetadataResponse {
        return GameMetadataResponse(
            store = emptyMap(),
            langOverride = emptyMap(),
            links = Links(
                wrapped = "https://wrapped.lunarclient.com/access/",
                store = "https://store.lunarclient.com/?utm_source=client-multiver&utm_medium=main-menu",
                youtooz = "https://lunarclient.youtooz.com",
                trailer = "https://lunarclient.com/trailer"
            ),
            serverIntegration = listOf(
                ServerIntegration(
                    ip = listOf("shotbow.net", "shotbow.com"),
                    modSettings = mapOf(
                        "textHotKey" to ModSetting(enabled = false),
                        "particleMod" to ModSetting(enabled = false)
                    )
                ),
                ServerIntegration(
                    ip = listOf("hypixel.net", "hypixel.io"),
                    brand = "Hypixel BungeeCord",
                    modSettings = mapOf(
                        "textHotKey" to ModSetting(enabled = false),
                        "freelook" to ModSetting(enabled = true),
                        "MINIMAP" to ModSetting(enabled = true),
                        "particleMod" to ModSetting(properties = mapOf("particleMod_footstep" to "false"))
                    )
                )
                // ...其余 serverIntegration 自行追加
            ),
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
                FeatureFlag("SentryIchorHandler", true),
                FeatureFlag("LunarPlus", true),
                FeatureFlag("DevSentryReportHandler", true),
                FeatureFlag("BetaSentryReportHandler", true)
                // ...省略一堆可追加的 FeatureFlag
            ),
            sentryFilteredExceptions = listOf(
                SentryFilter("rpc-timeout", "Timeout waiting for response .*"),
                SentryFilter("lcqt-1", "unlocker\\.S\\.send"),
                SentryFilter("lcqt-2", "Cannot invoke \\\"com\\.moonsworth.*?\\\" because.*?")
            ),
            starServers = listOf(
                StarServer("*.playskyward.gg"),
                StarServer("*.mchub.com"),
                StarServer("mc.minehut.com")
            ),
            blogPosts = listOf(
                BlogPost(
                    title = "Skyward4",
                    link = "https://discord.gg/8GuNA6zN4c",
                    image = "https://launcherimages.lunarclientcdn.com/…/SkywardsBillboard4-alt.png"
                ),
                BlogPost(
                    title = "MCL5`",
                    link = "https://www.hrblock.com/offers/lunar-client/",
                    image = "https://launcherimages.lunarclientcdn.com/…/MCLBillboard5.png"
                )
            ),
            alert = Alert(
                colors = mapOf(
                    "RED" to AlertColor("#59db4040", "#80db4040"),
                    "BLUE" to AlertColor("#592b71ce", "#802b71ce")
                ),
                active = AlertActive(
                    id = "729",
                    name = "Discord Giveaway",
                    text = "Join our Discord for news, giveaways, and more!",
                    color = "RED",
                    icon = "BULLHORN",
                    dismissable = true,
                    link = "https://discord.com/invite/LunarClient"
                )
            ),
            pinnedServers = listOf(
                PinnedServer(
                    name = "Complex Gaming",
                    ip = "lunar.mc-complex.com",
                    removable = true,
                    versions = listOf("1.8.9", "1.8", "1.12.2"),
                    expirationDate = -1L
                ),
                PinnedServer(
                    name = "Skyward",
                    ip = "lunar.playskyward.gg",
                    removable = true,
                    versions = listOf("1.8.9", "1.8", "1.12.2"),
                    expirationDate = -1L
                )
            )
        )

    }
}