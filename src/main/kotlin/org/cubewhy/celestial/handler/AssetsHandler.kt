package org.cubewhy.celestial.handler

import com.lunarclient.websocket.handshake.v1.WebsocketHandshakeV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.util.wrapCommon
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
data class AssetsHandler(
    val packetService: PacketService
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        return Mono.defer {
            if (!session.attributes.containsKey("user")) {
                handleHandshake(session)
            } else {
                handleCommon(session)
            }
        }
    }

    private fun handleHandshake(session: WebSocketSession): Mono<Void> {
        return session.receive()
            .flatMap { message -> mono { WebsocketHandshakeV1.Handshake.parseDelimitedFrom(message.payload.asInputStream()) } }
            .flatMap { message ->
                mono { packetService.processHandshake(message, session) }
            }
            .then()
    }

    private fun handleCommon(session: WebSocketSession): Mono<Void> {
        return session.receive()
            .flatMap { message -> mono { WebsocketProtocolV1.ServerboundWebSocketMessage.parseDelimitedFrom(message.payload.asInputStream()) } }
            .flatMap { message ->
                mono { packetService.process(message, session)?.wrapCommon(message.requestId) }
            }
            .flatMap { message ->
                session.send(session.binaryMessage { it.wrap(message.toByteArray()) }.toMono())
            } // convent message and send
            .then()
    }
}