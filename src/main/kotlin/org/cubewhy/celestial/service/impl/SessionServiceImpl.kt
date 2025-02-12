package org.cubewhy.celestial.service.impl

import com.google.protobuf.kotlin.toByteString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.Federation.FederationRequest
import org.cubewhy.celestial.entity.OnlineUser
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.handler.getSessionLocally
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.Const.SHARED_SESSION
import org.reactivestreams.Publisher
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setAndAwait
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
import java.util.function.Function

@Service
class SessionServiceImpl(
    private val onlineUserRedisTemplate: ReactiveRedisTemplate<String, OnlineUser>,
    private val dataBufferFactory: DataBufferFactory,
    private val rabbitStreamTemplate: RabbitStreamTemplate
) : SessionService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun saveSession(session: WebSocketSession) {
        val user = session.attributes["user"] as User
        val onlineUser = OnlineUser(user.uuid, session.id)
        logger.info { "Save ${user.username} to shared session store" }
        onlineUserRedisTemplate.opsForValue().setAndAwait(SHARED_SESSION + user.uuid, onlineUser)
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

    override suspend fun getSession(uuid: String): WebSocketSession? {
        val localSession = getSessionLocally(uuid)
        // find session locally
        if (localSession != null) {
            return localSession
        }
        // find session from redis
        val onlineUser = onlineUserRedisTemplate.opsForValue().getAndAwait(SHARED_SESSION + uuid) ?: return null
        return this.buildWebsocketSession(onlineUser)
    }

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
        throw UnsupportedOperationException("Shared websocket doesn't have attributes")
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