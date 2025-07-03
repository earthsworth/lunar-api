package org.cubewhy.celestial.event

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection
import org.springframework.context.ApplicationEvent

class UserOfflineEvent(source: Any, val user: User) : ApplicationEvent(source)

class UserSubscribeEvent(source: Any, val user: User, val uuids: List<String>, val connection: ClientConnection<*>) :
    ApplicationEvent(source)