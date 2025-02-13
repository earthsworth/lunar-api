package org.cubewhy.celestial.service.impl

import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.event.UserOfflineEvent
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Instant

@Service
data class UserServiceImpl(
    private val userRepository: UserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) : UserService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun loadUser(username: String, uuid: String): User {
        return userRepository.findByUuid(uuid)
            .switchIfEmpty {
                userRepository.save(
                    User(
                        username = username,
                        uuid = uuid,
                        role = Role.USER
                    )
                )
            }
            .doOnNext { user ->
                logger.info { "Successfully loaded user ${user.username}" }
            }
            .awaitFirst()
    }

    override suspend fun loadUser(hello: LunarclientAuthenticatorV1.HelloMessage): User {
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
}