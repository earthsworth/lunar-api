package org.cubewhy.celestial.service.impl

import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebUser
import org.cubewhy.celestial.entity.dto.RegisterUserDTO
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.event.UserOfflineEvent
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.repository.WebUserRepository
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.beans.factory.annotation.Value
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
    private val webUserRepository: WebUserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val passwordEncoder: PasswordEncoder,
    private val coroutineScope: CoroutineScope
) : UserService {
    @Value("\${lunar.user.default.username}")
    private lateinit var defaultUsername: String

    @Value("\${lunar.user.default.password}")
    private lateinit var defaultUserPassword: String

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    fun init() {
        coroutineScope.launch {
            // create default user
            if (webUserRepository.countByRole(Role.OWNER).awaitFirst() == 0L) {
                logger.info { "Creating default users" }
                val owner = WebUser(
                    username = "admin",
                    password = passwordEncoder.encode("password"),
                    role = Role.OWNER
                )
                webUserRepository.save(owner).awaitFirst()
                logger.warn { "Default user created. ${defaultUsername}:$defaultUserPassword" }
                logger.warn { "Please change it via the web dashboard!" }
            }
        }
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

    override suspend fun registerWebUser(dto: RegisterUserDTO): UserVO? {
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
        return UserVO(
            id = saved.id!!,
            username = saved.username,
            role = saved.role.name
        )
    }
}