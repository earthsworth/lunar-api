package org.cubewhy.celestial.handler.websocket

import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.protocol.WebsocketConnection
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.util.wrapAuthenticator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.netty.channel.AbortedException

@Component
class AuthorizeHandler(
    private val packetService: PacketService
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        // create the connection
        val connection: ClientConnection<WebSocketSession> = WebsocketConnection(session)

        return session.receive()
            .flatMap { message ->
                ServerboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                    .toMono()
            } // parse message
            .flatMap { message -> mono { packetService.processAuthorize(connection, message) } } // process message
            .flatMap { message -> message.wrapAuthenticator().toMono() } // wrap message
            .flatMap { message ->
                mono { connection.send(message) }
            } // convent message and send
            .doOnError { e ->
                if (e !is AbortedException) {
                    // ignore session disconnected
                    logger.error(e) { "WebSocket processing error" }
                }
            }
            .then()
    }
}