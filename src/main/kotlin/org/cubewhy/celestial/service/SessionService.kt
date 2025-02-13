package org.cubewhy.celestial.service

import com.lunarclient.common.v1.LunarclientCommonV1
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun getSession(uuid: String): WebSocketSession?
    suspend fun saveSession(user: User, session: WebSocketSession)
    suspend fun getSession(user: User): WebSocketSession?
    suspend fun removeSession(user: User)
    suspend fun saveMinecraftVersion(user: User, version: String)
    suspend fun saveLocation(user: User, location: WebsocketFriendV1.InboundLocation)
    suspend fun getMinecraftVersion(uuid: String): String?
    suspend fun getLocation(uuid: String): LunarclientCommonV1.Location?
}