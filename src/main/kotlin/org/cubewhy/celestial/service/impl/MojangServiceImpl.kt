package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.service.MojangService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

@Service
class MojangServiceImpl(
    private val webClient: WebClient
) : MojangService {
    override suspend fun hasJoined(username: String, serverId: String): Boolean {
        // request to https://sessionserver.mojang.com/session/minecraft/hasJoined?username=<player name>&serverId=<Server ID>&ip=<Client IP>
        return try {
            webClient
                .get()
                .uri("https://sessionserver.mojang.com/session/minecraft/hasJoined") { builder ->
                    builder
                        .queryParam("username", username)
                        .queryParam("serverId", serverId)
                        .build()
                }
                .retrieve()
                .awaitBodilessEntity().statusCode.is2xxSuccessful
        } catch (e: Exception) {
            false
        }
    }
}