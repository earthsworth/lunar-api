package org.cubewhy.celestial.handler.websocket

import com.lunarclient.websocket.handshake.v1.Handshake
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.protocol.WebsocketConnection
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.service.SessionService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.netty.channel.AbortedException
import java.util.concurrent.ConcurrentHashMap

@Component
class AssetsHandler(
    private val packetService: PacketService,
    private val sessionService: SessionService,
    private val userRepository: UserRepository,
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
        val sessions = ConcurrentHashMap<String, ClientConnection<*>>() // session-id:session
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        // create the connection
        val connection = WebsocketConnection(session)

        return session.receive().concatMap { message ->
            if (connection.metadata.userId == null) {
                // process handshake
                val pbMessage = Handshake.parseFrom(message.payload.asInputStream())
                mono {
                    val user = packetService.processHandshake(pbMessage, connection)
                    if (user == null) {
                        // unauthorized, close the session
                        connection.close(CloseStatus.NOT_ACCEPTABLE.code, CloseStatus.NOT_ACCEPTABLE.reason)
                        return@mono null
                    }
                    // put user id
                    connection.metadata.userId = user.id
                    sessions[session.id] = connection
                    null // no response
                }
            } else {
                // process common message
                val pbMessage =
                    ServerboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                mono {
                    packetService.process(pbMessage, connection).apply {
                        requestId = pbMessage.requestId
                    }
                }
            }
        }.concatMap { message ->
            mono {
                connection.sendResponse(message)

                // find user
                val userId = connection.metadata.userId!!
                val user = userRepository.findById(userId).awaitFirst()
                // send events
                message.pushes.forEach { push ->
                    if (push.broadcast) {
                        // push to all sessions that logged in the same account
                        sessionService.push(user, push.payload)
                    }
                }
            }
        }.doOnError { e ->
            if (e !is AbortedException) {
                // ignore session disconnected
                logger.error(e) { "WebSocket processing error" }
            }
        }.doFinally { signalType ->
            (connection.metadata.userId)?.let { userId ->
                // remove from local session store
                sessions.remove(session.id)
                // perform processDisconnect
                mono {
                    // find user
                    val user = userRepository.findById(userId).awaitFirst()
                    packetService.processDisconnect(signalType, connection, user)
                }.publishOn(Schedulers.boundedElastic()).subscribe()
            }
        }.then()
    }
}
