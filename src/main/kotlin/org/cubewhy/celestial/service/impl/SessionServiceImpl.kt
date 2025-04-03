package org.cubewhy.celestial.service.impl

import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.celestial.avro.FederationMessage
import org.cubewhy.celestial.entity.UserWebsocketSession
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.handler.getSessionLocally
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.Const
import org.cubewhy.celestial.util.Const.SHARED_SESSION
import org.cubewhy.celestial.util.toJson
import org.cubewhy.celestial.util.toProtobufMessage
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Duration
import java.time.Instant

@Service
class SessionServiceImpl(
    private val userWebsocketSessionReactiveRedisTemplate: ReactiveRedisTemplate<String, UserWebsocketSession>,
    private val streamBridge: StreamBridge,
    private val userRepository: UserRepository,
) : SessionService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun saveSession(user: User, websocketSession: WebSocketSession) {
        if (this.isOnSession(websocketSession, user)) {
            logger.warn { "Skipping save session of user ${user.username} on websocket connection ${websocketSession.id} (already exists)" }
            return
        }
        logger.info { "Saving session for ${user.username} at connection ${websocketSession.id}" }
        val wsSessionObject = UserWebsocketSession(
            websocketId = websocketSession.id,
            userId = user.id!!,
            userUuid = user.uuid
        )
        userWebsocketSessionReactiveRedisTemplate.opsForSet().add(Const.USER_WEBSOCKET_SESSION_STORE, wsSessionObject)
            .awaitFirst()
    }

    override suspend fun isOnSession(session: WebSocketSession, user: User): Boolean {
        return findSessions(user).any { it.websocketId == session.id }
    }

    override suspend fun isSessionValid(sessionId: String): Boolean {
        return sessionRepository.existsById(sessionId).awaitFirst()
    }

    override fun pushEvent(userId: String, event: GeneratedMessage) {
        // convert to avro
        val payload = FederationMessage.newBuilder().apply {
            this.userId = userId
            this.payload = protobufEventOf(event, userId).toByteString().asReadOnlyByteBuffer()
            this.timestamp = Instant.now().epochSecond
        }
        // send to broker
        if (!streamBridge.send("lunar-1", payload)) {
            logger.error { "Failed to push event to user $${userId}" }
        }
    }

    override suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit) {
        // find the user
        val user = userRepository.findById(userId).awaitFirst()
        // find all available sessions
        this.findSessions(user).toFlux().mapNotNull {
            // find on local session map
            WebsocketHandler.sessions[it.websocketId]
        }.flatMap { session ->
            mono { func.invoke(session!!) }
        }.awaitLast()
    }

    override suspend fun findSessions(user: User): List<UserWebsocketSession> =
        userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.userId == user.id }
            .collectList()
            .awaitLast()

    override suspend fun saveMinecraftVersion(user: User, version: String) {
        // get online user object
        getOnlineUser(user.uuid)?.let { onlineUser ->
            logger.info { "Player ${user.username}'s Minecraft version was set to $version" }
            onlineUser.minecraftVersion = version
            saveSession(onlineUser)
        }
    }

    override suspend fun getMinecraftVersion(uuid: String): String? {
        return getOnlineUser(uuid)?.minecraftVersion
    }

    override suspend fun saveLocation(user: User, location: InboundLocation) {
        // convert to json
        val locationJson = location.toJson()
        getOnlineUser(user.uuid)?.let { onlineUser ->
            onlineUser.location = locationJson
            saveSession(onlineUser)
        }
    }

    override suspend fun getLocation(uuid: String): Location? = getOnlineUser(uuid)?.let { onlineUser ->
        try {
            return onlineUser.location?.toProtobufMessage(Location.newBuilder())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Remove a session from shared session store
     *
     * @param user Player
     * */
    override suspend fun removeSession(user: User) {
        logger.info { "Remove ${user.username} from shared session store" }
        userWebsocketSessionRedisTemplate.opsForValue().deleteAndAwait(SHARED_SESSION + user.uuid)
    }

    private suspend fun saveSession(userWebsocketSession: UserWebsocketSession) {
        userWebsocketSessionRedisTemplate.opsForValue()
            .setAndAwait(SHARED_SESSION + userWebsocketSession.userUuid, userWebsocketSession, Duration.ofHours(12))
    }

    private suspend fun getOnlineUser(uuid: String): UserWebsocketSession? =
        userWebsocketSessionRedisTemplate.opsForValue().getAndAwait(SHARED_SESSION + uuid)

    override suspend fun countAvailableSessions(): Int {
        return userWebsocketSessionRedisTemplate.keys("$SHARED_SESSION*").collectList().awaitLast().count()
    }

    override suspend fun pushAll(func: suspend (User, WebSocketSession) -> Unit) {
        // find all sessions
        val sessionKeys = userWebsocketSessionRedisTemplate.keys("$SHARED_SESSION*").collectList().awaitLast()
        userWebsocketSessionRedisTemplate.opsForValue().multiGetAndAwait(sessionKeys).forEach { onlineUser ->
            // find user
            val user0 = userRepository.findByUuid(onlineUser!!.userUuid).awaitFirstOrNull()
            user0?.let { user ->
                // find session
                val session0 = this.getSession(onlineUser.userUuid)
                session0?.let { session ->
                    func.invoke(user, session)
                }
            }
        }
    }

    override suspend fun getSession(user: User) = this.getSession(user.uuid)
}
