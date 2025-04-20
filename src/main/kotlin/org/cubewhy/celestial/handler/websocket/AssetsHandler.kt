package org.cubewhy.celestial.handler.websocket

import com.lunarclient.websocket.handshake.v1.Handshake
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.pushEvent
import org.cubewhy.celestial.util.wrapCommon
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
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
        val sessions = ConcurrentHashMap<String, WebSocketSession>() // session-id:session
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.receive().concatMap { message ->
            if (!session.attributes.containsKey("user-id")) {
                // process handshake
                val pbMessage = Handshake.parseFrom(message.payload.asInputStream())
                mono {
                    val user = packetService.processHandshake(pbMessage, session)
                    if (user == null) {
                        // unauthorized, close the session
                        session.close().awaitFirstOrNull()
                        return@mono null
                    }
                    session.attributes["user-id"] = user.id
                    sessions[session.id] = session
                    null // no response
                }
            } else {
                // process common message
                val pbMessage =
                    ServerboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                mono {
                    packetService.process(pbMessage, session).apply {
                        requestId = pbMessage.requestId
                    }
                }
            }
        }.concatMap { message ->
            mono {
                message.response?.let { response ->
                    session.send(session.binaryMessage {
                        it.wrap(
                            response.wrapCommon(message.requestId!!).toByteArray()
                        )
                    }.toMono()).awaitFirstOrNull() // send response
                }
                // find user
                val userId = session.attributes["user-id"] as String
                val user = userRepository.findById(userId).awaitFirst()
                // send events
                message.pushes.forEach { push ->
                    if (!push.broadcast) {
                        session.pushEvent(push.payload)
                    } else {
                        // push to all sessions that logged in the same account
                        // find user
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
            (session.attributes["user-id"] as String?)?.let { userId ->
                // remove from local session store
                sessions.remove(session.id)
                // perform processDisconnect
                mono {
                    // find user
                    val user = userRepository.findById(userId).awaitFirst()
                    packetService.processDisconnect(signalType, session, user)
                }.publishOn(Schedulers.boundedElastic()).subscribe()
            }
        }.then()
    }
}
