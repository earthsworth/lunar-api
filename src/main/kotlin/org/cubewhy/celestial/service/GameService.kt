package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.GameMetadataResponse
import org.springframework.web.server.ServerWebExchange

interface GameService {
    suspend fun metadata(branch: String, exchange: ServerWebExchange): GameMetadataResponse
}