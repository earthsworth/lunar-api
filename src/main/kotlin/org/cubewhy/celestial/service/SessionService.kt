package org.cubewhy.celestial.service

import org.springframework.web.reactive.socket.WebSocketSession

interface SessionService {
    suspend fun getSession(uuid: String): WebSocketSession?
}