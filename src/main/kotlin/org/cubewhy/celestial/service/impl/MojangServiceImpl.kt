package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.service.MojangService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

@Service
class MojangServiceImpl(
    private val webClient: WebClient,
    private val lunarProps: LunarProperties,
) : MojangService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun hasJoined(username: String, serverId: String): Boolean {
        // https://minecraft.wiki/w/Mojang_API#Verify_login_session_on_client
        // request to https://sessionserver.mojang.com/session/minecraft/hasJoined?username=<player name>&serverId=<Server ID>&ip=<Client IP>
        return try {
            webClient
                .get()
                .uri(lunarProps.mojang.endpoints.hasJoined) { builder ->
                    builder
                        .queryParam("username", username)
                        .queryParam("serverId", serverId)
                        .build()
                }
                .retrieve()
                .awaitBodilessEntity().statusCode.is2xxSuccessful
        } catch (e: Exception) {
            logger.error(e) { "Failed to request Mojang API" }
            false
        }
    }
}