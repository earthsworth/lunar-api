package org.cubewhy.celestial.service.impl

import com.lunarclient.common.v1.UuidAndUsername
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.LogoColor
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.event.UserOfflineEvent
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.context.ApplicationEventPublisher
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
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val passwordEncoder: PasswordEncoder,
) : UserService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun loadUser(username: String, uuid: String): User {
        return userRepository.findByUuid(uuid)
            .switchIfEmpty {
                logger.info { "User with Minecraft username $username created" }
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
                    logger.info { "Update username ${user.username} -> $username" }
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

    override fun loadUserByUsername(username: String): Mono<User> {
        return userRepository.findByUsername(username)
    }

    override suspend fun loadUser(identity: UuidAndUsername): User {
        val uuid = identity.uuid.toUUIDString()
        return this.loadUser(identity.username, uuid)
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

    override suspend fun switchLogoColor(user: User, color: LogoColor) {
        // check permission
        if (!user.availableLogoColors.contains(color)) {
            throw IllegalStateException("No permission")
        }
        // apply changes
        logger.info { "User ${user.username} changed his logo color (${color.name})" }
        user.cosmetic.lunarLogoColor = color
        // save user
        userRepository.save(user).awaitFirst()
    }

    override suspend fun updatePassword(user: User, newPassword: String) {
        logger.info { "User ${user.username} updated his password" }
        user.password = passwordEncoder.encode(newPassword)
        // save user
        userRepository.save(user).awaitFirst()
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        // find the username in webUser repository
        return userRepository.findByUsername(username)
            .flatMap { webUser ->
                // build User details
                org.springframework.security.core.userdetails.User.builder()
                    .username(webUser.username)
                    .password(webUser.password)
                    .roles(*webUser.roles.map { "ROLE_" + it.name }.toTypedArray())
                    .build().toMono()
            }
    }
}