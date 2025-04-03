package org.cubewhy.celestial.service

import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun countAvailableSessions(): Int
    suspend fun saveSession(user: User, websocketSession: WebSocketSession)
    suspend fun getSession(user: User): WebSocketSession?
    suspend fun removeSession(user: User)
    suspend fun saveMinecraftVersion(user: User, version: String)
    suspend fun saveLocation(user: User, location: InboundLocation)
    suspend fun getMinecraftVersion(uuid: String): String?
    suspend fun getLocation(uuid: String): Location?
    suspend fun pushAll(func: suspend (User, WebSocketSession) -> Unit)
}