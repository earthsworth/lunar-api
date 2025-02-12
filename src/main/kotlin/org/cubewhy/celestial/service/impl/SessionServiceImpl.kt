package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.OnlineUser
import org.cubewhy.celestial.handler.getSessionLocally
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.Const.SHARED_SESSION
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.getAndAwait
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class SessionServiceImpl(
    private val onlineUserRedisTemplate: ReactiveRedisTemplate<String, OnlineUser>,
) : SessionService {
    // todo add session remove session

    override suspend fun getSession(uuid: String): WebSocketSession? {
        val localSession = getSessionLocally(uuid)
        // find session locally
        if (localSession != null) {
            return localSession
        }
        // find session from redis
        val onlineUser = onlineUserRedisTemplate.opsForValue().getAndAwait(SHARED_SESSION + uuid) ?: return null
        return this.buildWebsocketSession(onlineUser)
    }

    private suspend fun buildWebsocketSession(onlineUser: OnlineUser): WebSocketSession {
        TODO("wip")
    }
}