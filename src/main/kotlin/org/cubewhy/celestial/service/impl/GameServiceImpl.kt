package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.GameMetadataResponse
import org.cubewhy.celestial.service.GameService
import org.springframework.stereotype.Service

@Service
class GameServiceImpl : GameService {
    override suspend fun metadata(branch: String): GameMetadataResponse {
        return GameMetadataResponse(
            blogPosts = TODO(),
            alert = TODO(),
            modSettings = TODO(),
            clientSettings = TODO(),
            pinnedServers = TODO(),
            starServers = TODO(),
            featureFlag = TODO(),
            serverIntegration = TODO(),
            store = TODO(),
            sentryFilteredExceptions = TODO(),
            langOverride = TODO(),
            links = TODO()
        )
    }
}