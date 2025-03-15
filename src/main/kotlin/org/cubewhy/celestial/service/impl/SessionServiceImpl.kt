package org.cubewhy.celestial.service.impl

import com.google.protobuf.kotlin.toByteString
import com.lunarclient.common.v1.Location
import com.lunarclient.websocket.friend.v1.InboundLocation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.celestial.Federation.FederationRequest
import org.cubewhy.celestial.entity.OnlineUser
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.handler.getSessionLocally
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.Const.SHARED_SESSION
import org.cubewhy.celestial.util.toJson
import org.cubewhy.celestial.util.toProtobufMessage
import org.reactivestreams.Publisher
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.data.redis.core.*
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.function.Function

@Service
class SessionServiceImpl(
    private val onlineUserRedisTemplate: ReactiveRedisTemplate<String, OnlineUser>,
    private val dataBufferFactory: DataBufferFactory,
    private val rabbitStreamTemplate: RabbitStreamTemplate,
    private val userRepository: UserRepository,
) : SessionService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun saveSession(user: User, session: WebSocketSession) {
        // close exist connection
        getSessionLocally(user.uuid)?.let {
            logger.info { "Close existing session ${it.id} for user ${user.uuid}" }
            it.close().awaitFirstOrNull() // close session
        }
        val onlineUser = OnlineUser(user.uuid, session.id)
        logger.info { "Save ${user.username} to shared session store" }
        saveSession(onlineUser)
    }

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

    override suspend fun getLocation(uuid: String): Location? =
        getOnlineUser(uuid)?.let { onlineUser ->
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
        onlineUserRedisTemplate.opsForValue().deleteAndAwait(SHARED_SESSION + user.uuid)
    }

    private suspend fun saveSession(onlineUser: OnlineUser) {
        onlineUserRedisTemplate.opsForValue()
            .setAndAwait(SHARED_SESSION + onlineUser.userUuid, onlineUser, Duration.ofHours(12))
    }

    private suspend fun getOnlineUser(uuid: String): OnlineUser? =
        onlineUserRedisTemplate.opsForValue().getAndAwait(SHARED_SESSION + uuid)

    override suspend fun countAvailableSessions(): Int {
        return onlineUserRedisTemplate.keys("$SHARED_SESSION*").collectList().awaitLast().count()
    }

    override suspend fun pushAll(func: suspend (User, WebSocketSession) -> Unit) {
        // find all sessions
        val sessionKeys = onlineUserRedisTemplate.keys("$SHARED_SESSION*").collectList().awaitLast()
        onlineUserRedisTemplate.opsForValue().multiGetAndAwait(sessionKeys).forEach { onlineUser ->
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

    override suspend fun getSession(uuid: String): WebSocketSession? {
        val localSession = getSessionLocally(uuid)
        // find session locally
        if (localSession != null) {
            return localSession
        }
        // find session from redis
        val onlineUser = getOnlineUser(uuid) ?: return null
        return this.buildWebsocketSession(onlineUser)
    }

    override suspend fun getSession(user: User) = this.getSession(user.uuid)

    private suspend fun buildWebsocketSession(onlineUser: OnlineUser): FederationWebSocketSession {
        return FederationWebSocketSession(onlineUser, dataBufferFactory, rabbitStreamTemplate)
    }

    @RabbitListener(queues = ["stream.lunar.queue1"])
    private suspend fun listen(data: ByteArray) {
        // parse message
        val request = FederationRequest.parseFrom(data)
        val websocketSession = getSessionLocally(request.uuid)
        websocketSession?.let {
            // send payload to the session
            logger.info { "Received federation message (UUID: ${request.uuid})" }
            it.send(it.binaryMessage { factory ->
                factory.wrap(request.payload.toByteArray())
            }.toMono()).awaitFirstOrNull()
        }
    }
}

class FederationWebSocketSession(
    private val onlineUser: OnlineUser,
    private val dataBufferFactory: DataBufferFactory,
    private val rabbitStreamTemplate: RabbitStreamTemplate

) : WebSocketSession {
    override fun getId() = onlineUser.websocketId

    override fun getHandshakeInfo(): HandshakeInfo {
        throw UnsupportedOperationException("Shared websocket doesn't have handshakeInfo")
    }

    override fun bufferFactory(): DataBufferFactory {
        return this.dataBufferFactory
    }

    override fun getAttributes(): MutableMap<String, Any> {
        throw UnsupportedOperationException("Not implemented")
    }


    override fun receive(): Flux<WebSocketMessage> {
        throw UnsupportedOperationException("Shared websocket doesn't support receive messages on other clusters")
    }

    override fun send(messages: Publisher<WebSocketMessage>): Mono<Void> {
        return Flux.from(messages)
            .flatMap { message ->
                this.sendMessage(message)
            }
            .then()
    }

    /**
     * Send packet to other instances
     * */
    private fun sendMessage(message: WebSocketMessage): Mono<Void> {
        // read data
        val bytes = ByteArray(message.payload.readableByteCount())
        message.payload.read(bytes)
        val payload = FederationRequest.newBuilder()
            .setUuid(this.onlineUser.userUuid)
            .setPayload(bytes.toByteString())
        return Mono
            .fromFuture(rabbitStreamTemplate.convertAndSend(payload.build().toByteArray()))
            .then()
    }

    override fun isOpen() = true

    override fun close(status: CloseStatus): Mono<Void> {
        throw UnsupportedOperationException("Shared websocket should be closed at the target cluster")
    }

    override fun closeStatus(): Mono<CloseStatus> {
        throw UnsupportedOperationException("Shared websocket should be closed at the target cluster")
    }

    override fun textMessage(payload: String): WebSocketMessage {
        val bytes = payload.toByteArray(StandardCharsets.UTF_8)
        val buffer = bufferFactory().wrap(bytes)
        return WebSocketMessage(WebSocketMessage.Type.TEXT, buffer)
    }

    override fun binaryMessage(payloadFactory: Function<DataBufferFactory, DataBuffer>): WebSocketMessage {
        val payload = payloadFactory.apply(bufferFactory())
        return WebSocketMessage(WebSocketMessage.Type.BINARY, payload)
    }

    override fun pingMessage(payloadFactory: Function<DataBufferFactory, DataBuffer>): WebSocketMessage {
        val payload = payloadFactory.apply(bufferFactory())
        return WebSocketMessage(WebSocketMessage.Type.PING, payload)
    }

    override fun pongMessage(payloadFactory: Function<DataBufferFactory, DataBuffer>): WebSocketMessage {
        val payload = payloadFactory.apply(bufferFactory())
        return WebSocketMessage(WebSocketMessage.Type.PONG, payload)
    }

}