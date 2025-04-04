package org.cubewhy.celestial.service.impl

import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.avro.FederationMessage
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.UserWebsocketSession
import org.cubewhy.celestial.handler.AssetsHandler
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.Const
import org.cubewhy.celestial.util.Const.SHARED_SESSION
import org.cubewhy.celestial.util.toJson
import org.cubewhy.celestial.util.toProtobufMessage
import org.cubewhy.celestial.util.wrapPush
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toFlux
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

    override fun push(user: User, push: GeneratedMessage) {
        this.push(user.id!!, push)
    }

    override suspend fun isOnline(user: User): Boolean {
        return userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .any { it.userId == user.id!! }
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

    override suspend fun processWithSessionLocally(userId: String, func: suspend (WebSocketSession) -> Unit) {
        // find the user
        val user = userRepository.findById(userId).awaitFirst()
        // find all available sessions
        this.findSessions(user).toFlux().mapNotNull {
            // find on local session map
            AssetsHandler.sessions[it.websocketId]
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
     * @param session websocket session
     * */
    override suspend fun removeSession(session: WebSocketSession) {
        this.getUserSession(session)?.let { userWebsocketSession ->
            val user = userRepository.findById(userWebsocketSession.userId).awaitFirst()
            logger.info { "Remove ${user.username} from shared session store" }
            userWebsocketSessionReactiveRedisTemplate.opsForSet().remove(Const.USER_WEBSOCKET_SESSION_STORE, userWebsocketSession)
        }
    }

    override suspend fun getUserSession(session: WebSocketSession): UserWebsocketSession? {
        return userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .filter { it.websocketId == session.id }
            .awaitLast()
    }

    override suspend fun countAvailableSessions(): Long {
        return userWebsocketSessionReactiveRedisTemplate.opsForSet().scan(Const.USER_WEBSOCKET_SESSION_STORE).count()
            .awaitLast()
    }

    override suspend fun pushAll(func: suspend (User, WebSocketSession) -> Unit) {
        // find all sessions
        userWebsocketSessionReactiveRedisTemplate.opsForSet()
            .scan(Const.USER_WEBSOCKET_SESSION_STORE)
            .collectList()
            .awaitLast()
            .forEach { onlineUser ->
                // find user
                userRepository.findByUuid(onlineUser!!.userUuid).awaitFirstOrNull()?.let { user ->
                    // find session
                    this.processWithSessionLocally(user.id!!) { session ->
                        func.invoke(user, session)
                    }
                }
            }
    }
}
