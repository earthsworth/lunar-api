package org.cubewhy.celestial.handler

import com.lunarclient.websocket.handshake.v1.WebsocketHandshakeV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import io.github.oshai.kotlinlogging.KotlinLogging
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
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.receive().concatMap { message ->
            if (!session.attributes.containsKey("user")) {
                val pbMessage = WebsocketHandshakeV1.Handshake.parseFrom(message.payload.asInputStream())
                mono {

                    // process handshake

                    session.attributes["user"] = packetService.processHandshake(pbMessage, session)
                    null // no response
                }
            } else {
                val pbMessage = WebsocketProtocolV1.ServerboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                mono {
                    packetService.process(pbMessage, session)?.wrapCommon(pbMessage.requestId)

                }
            }
        }.concatMap { message ->
            session.send(session.binaryMessage { it.wrap(message.toByteArray()) }.toMono()) // send response
        }.doOnError { err ->
            logger.error(err) { "Failed to handle websocket message" }
        }.doFinally { signalType ->
            // remove session id and close session
            logger.info { "Websocket terminated [${signalType.name}]" }
        }.then()
    }
}