package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.service.UserMapper
import org.springframework.stereotype.Service

@Service
class UserMapperImpl : UserMapper {
    override fun mapToUserVO(user: User) = UserVO(
        id = user.id!!,
        username = user.username,
        uuid = user.uuid,
    )
}