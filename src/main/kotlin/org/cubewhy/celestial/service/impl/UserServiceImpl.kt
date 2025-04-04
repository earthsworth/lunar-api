package org.cubewhy.celestial.service.impl

import com.lunarclient.authenticator.v1.HelloMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebUser
import org.cubewhy.celestial.entity.dto.RegisterUserDTO
import org.cubewhy.celestial.entity.dto.ResetPasswordDTO
import org.cubewhy.celestial.entity.vo.WebUserVO
import org.cubewhy.celestial.event.UserOfflineEvent
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.repository.WebUserRepository
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val webUserRepository: WebUserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val passwordEncoder: PasswordEncoder,
    private val coroutineScope: CoroutineScope
) : UserService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun loadUser(username: String, uuid: String): User {
        return userRepository.findByUuid(uuid)
            .switchIfEmpty {
                logger.info { "User with Minecraft IGN $username created" }
                userRepository.save(
                    User(
                        username = username,
                        uuid = uuid,
                        roles = mutableListOf(Role.USER)
                    )
                )
            }
            .flatMap { user ->
                if (user.username != username) {
                    // user updated it's IGN
                    user.username = username // update username in database
                    return@flatMap userRepository.save(user)
                }
                user.toMono()
            }
            .doOnNext { user ->
                logger.info { "Successfully loaded user ${user.username}" }
            }
            .awaitFirst()
    }

    override suspend fun loadUser(hello: HelloMessage): User {
        val uuid = hello.identity.uuid.toUUIDString()
        return this.loadUser(hello.identity.username, uuid)
    }

    override suspend fun loadUserByUuid(uuid: String): User {
        return userRepository.findByUuid(uuid).awaitFirst()
    }

    override suspend fun markOffline(user: User) {
        user.lastSeenAt = Instant.now() // set offline timestamp
        // push events to friends
        applicationEventPublisher.publishEvent(UserOfflineEvent(this, user))
        userRepository.save(user).awaitFirst()
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        // find the username in webUser repository
        return webUserRepository.findByUsername(username)
            .flatMap { webUser ->
                // build User details
                org.springframework.security.core.userdetails.User.builder()
                    .username(webUser.username)
                    .password(webUser.password)
                    .roles(webUser.role.toString())
                    .build().toMono()
            }
    }

    override fun loadWebUser(username: String): Mono<WebUser> {
        return webUserRepository.findByUsername(username)
    }

    override suspend fun registerWebUser(dto: RegisterUserDTO): WebUserVO? {
        // create web user
        if (webUserRepository.existsByUsername(dto.username).awaitFirst()) {
            return null
        }
        val webUser = WebUser(
            username = dto.username,
            password = passwordEncoder.encode(dto.password),
            role = Role.USER
        )
        // save web user
        val saved = webUserRepository.save(webUser).awaitFirst()
        logger.info { "Web user ${webUser.username} was registered" }
        return WebUserVO(
            id = saved.id!!,
            username = saved.username,
            role = saved.role.name
        )
    }

    override suspend fun resetWebUserPassword(dto: ResetPasswordDTO, authentication: Authentication) {
        // check old password
        val user = webUserRepository.findByUsername(authentication.name).awaitFirst()
        if (!passwordEncoder.matches(dto.oldPassword, user.password)) {
            // password not match
            throw IllegalArgumentException("Old password does not match new password")
        }
        if (dto.oldPassword == dto.password) {
            throw IllegalArgumentException("Password cannot be same")
        }
        // update password
        logger.info { "Update password for web user ${user.username}" }
        user.password = passwordEncoder.encode(dto.password)
        // save user
        webUserRepository.save(user).awaitFirst()
    }

    override suspend fun loadWebUserVO(id: String): WebUserVO {
        val wu = webUserRepository.findById(id).awaitFirst()
        return WebUserVO(
            id = wu.id!!,
            username = wu.username,
            role = wu.role.name
        )
    }
}