package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.UserWebsocketSession
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun countAvailableSessions(): Long
    suspend fun saveSession(user: User, websocketSession: WebSocketSession)
    suspend fun getUserSession(session: WebSocketSession): UserWebsocketSession?
    suspend fun removeSession(session: WebSocketSession)
    suspend fun saveMinecraftVersion(user: User, version: String)
    suspend fun saveLocation(user: User, location: InboundLocation)
    suspend fun getMinecraftVersion(uuid: String): String?
    suspend fun getLocation(uuid: String): Location?
    suspend fun pushAll(func: suspend (User, WebSocketSession) -> Unit)
    suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit)
    fun push(userId: String, push: GeneratedMessage)
    suspend fun isOnSession(session: WebSocketSession, user: User): Boolean
    suspend fun findSessions(user: User): List<UserWebsocketSession>
    fun push(user: User, push: GeneratedMessage)
    suspend fun isOnline(user: User): Boolean
}