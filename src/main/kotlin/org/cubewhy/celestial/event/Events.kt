package org.cubewhy.celestial.event

import org.cubewhy.celestial.entity.User
import org.springframework.context.ApplicationEvent
import org.springframework.web.reactive.socket.WebSocketSession

class UserOfflineEvent(source: Any, val user: User) : ApplicationEvent(source)

class UserSubscribeEvent(source: Any, val user: User, val uuids: List<String>, val session: WebSocketSession) :
    ApplicationEvent(source)