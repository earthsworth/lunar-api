package org.cubewhy.celestial.service

import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import org.cubewhy.celestial.entity.User

interface UserService {
    suspend fun loadUser(hello: LunarclientAuthenticatorV1.HelloMessage): User
    suspend fun loadUser(username: String, uuid: String): User
    suspend fun loadUserByUuid(uuid: String): User
    suspend fun markOffline(user: User)
}