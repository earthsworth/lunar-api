package org.cubewhy.celestial.event

import org.cubewhy.celestial.entity.User
import org.springframework.context.ApplicationEvent

class UserOfflineEvent(source: Any, val user: User) : ApplicationEvent(source)

class UserJoinWorldEvent(source: Any, val uuids: List<String>) : ApplicationEvent(source)