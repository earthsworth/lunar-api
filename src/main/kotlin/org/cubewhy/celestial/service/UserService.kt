package org.cubewhy.celestial.service

import com.lunarclient.authenticator.v1.HelloMessage
import org.cubewhy.celestial.entity.LogoColor
import org.cubewhy.celestial.entity.User
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import reactor.core.publisher.Mono

interface UserService : ReactiveUserDetailsService {
    suspend fun loadUser(hello: HelloMessage): User
    suspend fun loadUser(username: String, uuid: String): User

    fun loadUserByUsername(username: String): Mono<User>
    suspend fun loadUserByUuid(uuid: String): User
    suspend fun markOffline(user: User)
    suspend fun switchLogoColor(user: User, color: LogoColor)
    suspend fun updatePassword(user: User, newPassword: String)
}