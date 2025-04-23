package org.cubewhy.celestial.service.impl

import com.lunarclient.common.v1.UuidAndUsername
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.LogoColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.entity.dto.UpdatePasswordDTO
import org.cubewhy.celestial.entity.vo.PlayerInfoVO
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO
import org.cubewhy.celestial.event.UserOfflineEvent
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.UserMapper
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper,
    private val sessionService: SessionService,
    private val lunarProperties: LunarProperties
) : UserService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun loadUser(username: String, uuid: String): User {
        return userRepository.findByUuid(uuid)
            .switchIfEmpty {
                logger.info { "User with Minecraft username $username created" }
                userRepository.save(User(username = username, uuid = uuid))
            }
            .flatMap { user ->
                if (user.username != username) {
                    // user updated it's IGN
                    logger.info { "Update username ${user.username} -> $username" }
                    user.username = username // update username in database
                    return@flatMap userRepository.save(user)
                }
                // grant roles
                lunarProperties.user.roleAssignments.find { it.uuid == UUID.fromString(user.uuid) }?.let { roleAssignment ->
                    // find missing roles
                    val missingRoles = roleAssignment.roles.filterNot { user.roles.contains(it) }
                    if (missingRoles.isNotEmpty()) {
                        // add roles
                        logger.info { "Add missing roles to user ${user.username} ${missingRoles}" }
                        user.roles.addAll(missingRoles)
                        return@flatMap userRepository.save(user)
                    }
                }
                user.toMono()
            }
            .doOnNext { user ->
                logger.debug { "Successfully loaded user ${user.username}" }
            }
            .awaitFirst()
    }

    override suspend fun loadStyngrUser(authentication: Authentication, exchange: ServerWebExchange): StyngrUserVO {
        // find user
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        return userMapper.mapToStyngrUserVO(user, exchange)
    }

    override suspend fun getPlayerInfo(playerName: String): PlayerInfoVO {
        val target = userRepository.findByUsernameIgnoreCase(playerName).awaitFirst()
        val res = PlayerInfoVO(
            user = playerName,
            online = sessionService.isOnline(target),
            mcName = target.username,
            mcUuid = target.uuid,
            roleColor = target.cosmetic.lunarLogoColor.color,
            roles = target.resolvedRoles,
            plus = target.cosmetic.lunarPlusState,
        )
        return res
    }

    override suspend fun getUserRoles(username: String): List<String> {
        val user =
            userRepository.findByUsernameIgnoreCase(username).awaitFirstOrNull() ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User with username $username not found"
            )
        return user.resolvedRoles.map { it.name }
    }

    override suspend fun selfInfo(authentication: Authentication): UserVO {
        // find user
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        // map to VO
        return userMapper.mapToUserVO(user)
    }

    override fun loadUserByUsername(username: String): Mono<User> {
        return userRepository.findByUsernameIgnoreCase(username)
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

    override suspend fun updatePassword(
        authentication: Authentication,
        dto: UpdatePasswordDTO
    ) {
        // find user
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        if (user.password == null) {
            throw ResponseStatusException(
                HttpStatusCode.valueOf(400),
                "You doesn't have a password, please set it via lunar_bot"
            )
        }
        // verify old password
        if (!(passwordEncoder.matches(dto.oldPassword, user.password))) {
            throw ResponseStatusException(HttpStatusCode.valueOf(400), "The old password didn't match.")
        }
        // change password
        user.password = passwordEncoder.encode(dto.newPassword)
        // save user
        userRepository.save(user).awaitFirst()
    }

    override suspend fun updatePassword(user: User, newPassword: String) {
        // change password
        user.password = passwordEncoder.encode(newPassword)
        // save user
        userRepository.save(user).awaitFirst()
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        // find the username in webUser repository
        return userRepository.findByUsernameIgnoreCase(username)
            .flatMap { user ->
                // build User details
                org.springframework.security.core.userdetails.User.builder()
                    .username(user.username)
                    .password(user.password ?: return@flatMap Mono.empty())
                    .roles(*user.resolvedRoles.map { it.name }.toTypedArray())
                    .build().toMono()
            }
    }
}