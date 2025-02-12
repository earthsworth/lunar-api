package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun getSession(uuid: String): WebSocketSession?
    suspend fun saveSession(user: User, session: WebSocketSession)
    suspend fun getSession(user: User): WebSocketSession?
    suspend fun removeSession(user: User)
}