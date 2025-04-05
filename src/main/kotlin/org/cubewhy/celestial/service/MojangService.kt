package org.cubewhy.celestial.service

interface MojangService {
    suspend fun hasJoined(username: String, serverId: String): Boolean
}