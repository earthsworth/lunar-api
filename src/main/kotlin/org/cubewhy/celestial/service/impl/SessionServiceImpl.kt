package org.cubewhy.celestial.service.impl

import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.avro.FederationMessage
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.UserSession
import org.cubewhy.celestial.entity.config.InstanceProperties
import org.cubewhy.celestial.handler.websocket.AssetsHandler
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.Const
import org.cubewhy.celestial.util.wrapPush
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.removeAndAwait
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant

@Service
class SessionServiceImpl(
    private val userSessionReactiveRedisTemplate: ReactiveRedisTemplate<String, UserSession>,
    private val streamBridge: StreamBridge,
    private val userRepository: UserRepository,
    private val instanceProperties: InstanceProperties,
    private val scope: CoroutineScope
) : SessionService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun clearGhostSessions() {
        scope.launch {
            userSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
                .filter { it.instanceId == instanceProperties.id || it.instanceId == null }
                .collectList()
                .awaitLast()
                .forEach { ghostSession ->
                    logger.info { "Remove ghost session ${ghostSession.websocketId}" }
                    userSessionReactiveRedisTemplate
                        .opsForSet()
                        .removeAndAwait(Const.USER_WEBSOCKET_SESSION_STORE, ghostSession)
                }
        }
    }

    override suspend fun saveSession(user: User, connection: ClientConnection<*>) {
        if (this.isOnSession(connection, user)) {
            logger.warn { "Skipping save session of user ${user.username} on websocket connection ${connection.id} (already exists)" }
            return
        }
        logger.debug { "Saving session for ${user.username} at connection ${connection.id}" }
        val wsSessionObject = UserSession(
            websocketId = connection.id,
            userId = user.id!!,
            userUuid = user.uuid,
            instanceId = instanceProperties.id
        )
        userSessionReactiveRedisTemplate.opsForSet().add(Const.USER_WEBSOCKET_SESSION_STORE, wsSessionObject)
            .awaitFirst()
    }

    override suspend fun isOnSession(connection: ClientConnection<*>, user: User): Boolean {
        return findSessions(user).any { it.websocketId == connection.id }
    }

    override fun push(user: User, push: GeneratedMessage) {
        this.push(user.id!!, push)
    }

    override suspend fun isOnline(user: User): Boolean {
        return userSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .any { it.userId == user.id!! }
            .awaitFirst()
    }

    override suspend fun isOnline(uuid: String): Boolean {
        return userSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .any { it.userUuid == uuid }
            .awaitFirst()
    }

    override fun push(userId: String, push: GeneratedMessage) {
        // convert to avro
        val payload = FederationMessage.newBuilder().apply {
            this.userId = userId
            this.payload = push.wrapPush().toByteString().asReadOnlyByteBuffer()
            this.timestamp = Instant.now().epochSecond
        }
        // send to broker
        if (!streamBridge.send("lunar-1", payload)) {
            logger.error { "Failed to push event to user $${userId}" }
        }
    }

    override suspend fun processWithSessionLocally(
        userId: String,
        func: suspend (connection: ClientConnection<*>) -> Unit
    ) {
        // If user offline, make func stop
        if (!isOnline(userId)) return
        // find the user
        val user = userRepository.findById(userId).awaitFirst()
        // find all available sessions
        this.findSessions(user)
            .toFlux()
            .mapNotNull {
                // find on local session map
                AssetsHandler.sessions[it.websocketId]
            }
            .flatMap { connection ->
                mono { func.invoke(connection!!) }
            }
            .collectList()
            .awaitFirstOrNull()
    }

    override suspend fun findSessions(user: User): List<UserSession> =
        userSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.userId == user.id }
            .collectList()
            .awaitLast()

    override suspend fun saveMinecraftVersion(user: User, version: String) {
        // get online user object
        // TODO Save Minecraft version
    }

    override suspend fun getMinecraftVersion(uuid: String): String? {
        // TODO Get MC Version
        return null
    }

    override suspend fun saveLocation(user: User, location: InboundLocation) {
        // convert to json
//        val locationJson = location.toJson()
//        getOnlineUser(user.uuid)?.let { onlineUser ->
//            onlineUser.location = locationJson
//            saveSession(onlineUser)
//        }
        // TODO Get location
    }

    override suspend fun getLocation(uuid: String): Location? {
        // TODO get location
        return null
    }

    /**
     * Remove a session from shared session store
     *
     * @param connection websocket session
     * */
    override suspend fun removeSession(connection: ClientConnection<*>) {
        this.getUserSession(connection)?.let { session ->
            val user = userRepository.findById(session.userId).awaitFirst()
            logger.debug { "Remove ${user.username} from shared session store" }
            userSessionReactiveRedisTemplate.opsForSet()
                .removeAndAwait(Const.USER_WEBSOCKET_SESSION_STORE, session)
        }
    }

    override suspend fun getUserSession(connection: ClientConnection<*>): UserSession? {
        return userSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.websocketId == connection.id }
            .awaitLast()
    }

    override suspend fun countAvailableSessions(): Long {
        return userSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE).count()
            .awaitLast()
    }

    override suspend fun pushAll(func: suspend (User) -> Unit) {
        // find all sessions
        userSessionReactiveRedisTemplate.opsForSet()
            .scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .collectList()
            .awaitLast()
            .forEach { onlineUser ->
                // find user
                userRepository.findByUuid(onlineUser!!.userUuid).awaitFirstOrNull()?.let { user ->
                    // process with session
                    func.invoke(user)
                }
            }
    }
}
