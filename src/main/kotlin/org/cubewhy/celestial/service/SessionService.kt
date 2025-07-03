package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.UserSession
import org.cubewhy.celestial.protocol.ClientConnection

interface SessionService {
    suspend fun countAvailableSessions(): Long
    suspend fun saveSession(user: User, connection: ClientConnection<*>)
    suspend fun getUserSession(connection: ClientConnection<*>): UserSession?
    suspend fun removeSession(connection: ClientConnection<*>)
    suspend fun saveMinecraftVersion(user: User, version: String)
    suspend fun saveLocation(user: User, location: InboundLocation)
    suspend fun getMinecraftVersion(uuid: String): String?
    suspend fun getLocation(uuid: String): Location?
    suspend fun pushAll(func: suspend (User) -> Unit)
    suspend fun processWithSessionLocally(userId: String, func: suspend (ClientConnection<*>) -> Unit)
    fun push(userId: String, push: GeneratedMessage)
    suspend fun isOnSession(connection: ClientConnection<*>, user: User): Boolean
    suspend fun findSessions(user: User): List<UserSession>
    fun push(user: User, push: GeneratedMessage)
    suspend fun isOnline(user: User): Boolean
    suspend fun isOnline(uuid: String): Boolean
}