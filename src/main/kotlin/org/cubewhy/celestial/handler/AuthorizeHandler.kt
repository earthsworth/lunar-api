package org.cubewhy.celestial.handler

import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.util.wrapAuthenticator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
data class AuthorizeHandler(
    private val packetService: PacketService
) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        return session.receive()
            .flatMap { message -> mono { LunarclientAuthenticatorV1.ServerboundWebSocketMessage.parseDelimitedFrom(message.payload.asInputStream()) } } // parse message
            .flatMap { message -> mono { packetService.processAuthorize(message) } } // process message
            .flatMap { message -> message.wrapAuthenticator().toMono() } // wrap message
            .flatMap { message -> session.send(session.binaryMessage { it.wrap(message.toByteArray()) }.toMono()) } // convent message and send
            .then()
    }
}