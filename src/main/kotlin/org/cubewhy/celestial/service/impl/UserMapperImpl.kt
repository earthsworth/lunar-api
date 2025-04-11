package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO
import org.cubewhy.celestial.service.UserMapper
import org.cubewhy.celestial.util.JwtUtil
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange

@Service
class UserMapperImpl(private val jwtUtil: JwtUtil) : UserMapper {
    override fun mapToUserVO(user: User) = UserVO(
        id = user.id!!,
        username = user.username,
        uuid = user.uuid,
    )

    override fun mapToStyngrUserVO(user: User, exchange: ServerWebExchange): StyngrUserVO {
        val jwt = jwtUtil.convertToken(exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) as String)!!
        return StyngrUserVO(
            id = user.id!!,
            inGameCurrencyAmount = 9999.99,
            inGameCurrencyUrl = "https://lunarclient.top",
            accessToken = jwt
        )
    }
}