package org.cubewhy.celestial.service

import com.lunarclient.authenticator.v1.HelloMessage
import org.cubewhy.celestial.entity.LogoColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebUser
import org.cubewhy.celestial.entity.dto.RegisterUserDTO
import org.cubewhy.celestial.entity.dto.ResetPasswordDTO
import org.cubewhy.celestial.entity.vo.WebUserVO
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import reactor.core.publisher.Mono

interface UserService : ReactiveUserDetailsService {
    suspend fun loadUser(hello: HelloMessage): User
    suspend fun loadUser(username: String, uuid: String): User

    fun loadWebUser(username: String): Mono<WebUser>

    suspend fun loadUserByUuid(uuid: String): User
    suspend fun markOffline(user: User)
    suspend fun registerWebUser(dto: RegisterUserDTO): WebUserVO?
    suspend fun resetWebUserPassword(dto: ResetPasswordDTO, authentication: Authentication)
    suspend fun loadWebUserVO(id: String): WebUserVO
    suspend fun switchLogoColor(user: User, color: LogoColor)
    suspend fun updatePassword(user: User, newPassword: String)
}