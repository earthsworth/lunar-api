package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO
import org.cubewhy.celestial.service.UserMapper
import org.cubewhy.celestial.util.JwtUtil
import org.springframework.stereotype.Service

@Service
class UserMapperImpl(private val jwtUtil: JwtUtil) : UserMapper {
    override fun mapToUserVO(user: User) = UserVO(
        id = user.id!!,
        username = user.username,
        uuid = user.uuid,
        roles = user.resolvedRoles.map { it.name },
        logoColor = user.cosmetic.lunarLogoColor.color
    )

    override fun mapToStyngrUserVO(user: User, headerToken: String): StyngrUserVO {
        val jwt = jwtUtil.convertToken(headerToken)!!
        return StyngrUserVO(
            id = user.id!!,
            inGameCurrencyAmount = 9999.99,
            inGameCurrencyUrl = "https://lunarclient.top",
            accessToken = jwt
        )
    }
}