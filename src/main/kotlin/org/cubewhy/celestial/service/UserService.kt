package org.cubewhy.celestial.service

import com.lunarclient.common.v1.UuidAndUsername
import org.cubewhy.celestial.entity.LogoColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.vo.UserVO
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import reactor.core.publisher.Mono

interface UserService : ReactiveUserDetailsService {
    suspend fun loadUser(identity: UuidAndUsername): User
    suspend fun loadUser(username: String, uuid: String): User

    fun loadUserByUsername(username: String): Mono<User>
    suspend fun loadUserByUuid(uuid: String): User
    suspend fun markOffline(user: User)
    suspend fun switchLogoColor(user: User, color: LogoColor)
    suspend fun updatePassword(user: User, newPassword: String)

    suspend fun selfInfo(authentication: Authentication): UserVO
}