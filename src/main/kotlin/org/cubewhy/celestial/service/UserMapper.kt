package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO

interface UserMapper {
    fun mapToUserVO(user: User): UserVO
}