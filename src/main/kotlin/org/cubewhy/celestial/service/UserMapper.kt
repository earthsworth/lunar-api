package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO

interface UserMapper {
    fun mapToUserVO(user: User): UserVO
    fun mapToStyngrUserVO(user: User): StyngrUserVO
}