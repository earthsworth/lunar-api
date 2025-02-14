package org.cubewhy.celestial.handler

import com.lunarclient.websocket.handshake.v1.WebsocketHandshakeV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.handler.AssetsHandler.Companion.sessions
import org.cubewhy.celestial.service.PacketService
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
data class AssetsHandler(
    private val packetService: PacketService
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
        val sessions = ConcurrentHashMap<String, WebSocketSession>() // uuid:session
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.receive().concatMap { message ->
            if (!session.attributes.containsKey("user")) {
                // process handshake
                val pbMessage = WebsocketHandshakeV1.Handshake.parseFrom(message.payload.asInputStream())
                mono {
                    val user = packetService.processHandshake(pbMessage, session)
                    session.attributes["user"] = user
                    sessions[user!!.uuid] = session
                    null // no response
                }
            } else {
                // process common message
                val pbMessage =
                    WebsocketProtocolV1.ServerboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                mono {
                    packetService.process(pbMessage, session).apply {
                        requestId = pbMessage.requestId
                    }
                }
            }
        }.concatMap { message ->
            mono {
                message.response?.let { response ->
                    session.send(session.binaryMessage { it.wrap(response.wrapCommon(message.requestId!!).toByteArray()) }.toMono()).awaitFirstOrNull() // send response
                }
                // send events
                message.events.forEach { event ->
                    session.pushEvent(event)
                }
            }
        }.doOnError { e ->
            if (e !is AbortedException) {
                // ignore session disconnected
                logger.error(e) { "WebSocket processing error" }
            }
        }.doFinally { signalType ->
            // remove session id and close session
            val user = session.attributes["user"] as User?
            // remove session
            user?.let {
                // remove from local session store
                sessions.remove(it.uuid)
                // perform processDisconnect
                mono {
                    packetService.processDisconnect(signalType, session, it)
                }.publishOn(Schedulers.boundedElastic()).subscribe()
            }
        }.then()
    }
}

fun getSessionLocally(uuid: String): WebSocketSession? {
    return sessions[uuid]?.takeIf { it.isOpen }
}