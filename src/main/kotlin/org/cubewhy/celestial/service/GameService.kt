package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.GameMetadataResponse

interface GameService {
    suspend fun metadata(branch: String, baseUrl: String): GameMetadataResponse
}