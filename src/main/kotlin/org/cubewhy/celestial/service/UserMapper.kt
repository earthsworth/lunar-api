package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO
import org.springframework.web.server.ServerWebExchange

interface UserMapper {
    fun mapToUserVO(user: User): UserVO
    fun mapToStyngrUserVO(user: User, exchange: ServerWebExchange): StyngrUserVO
}