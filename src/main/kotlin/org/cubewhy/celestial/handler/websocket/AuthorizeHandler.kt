package org.cubewhy.celestial.handler.websocket

import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.protocol.WebsocketConnection
import org.cubewhy.celestial.service.ExtendService
import org.cubewhy.celestial.service.PacketService
import org.cubewhy.celestial.util.wrapAuthenticator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import reactor.netty.channel.AbortedException

@Component
class AuthorizeHandler(
    private val lunarProps: LunarProperties,
    private val packetService: PacketService,
    private val extendService: ExtendService,
) : WebSocketHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        // create the connection
        val connection: ClientConnection<WebSocketSession> = WebsocketConnection(session)

        var upstream: ClientConnection<*>? = null
        return session.receive()
            .flatMap { message ->
                ServerboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                    .toMono()
            } // parse message
            .flatMap { message ->
                mono {
                    if (upstream == null && lunarProps.upstream.enabled) {
                        upstream = extendService.openAuthConnection()
                    }
                    packetService.processAuthorize(connection, message, upstream)
                }
            } // process message
            .flatMap { message -> message.wrapAuthenticator().toMono() } // wrap message
            .flatMap { message ->
                mono { connection.send(message) }
            } // convent message and send
            .doOnError { e ->
                if (e !is AbortedException) {
                    // ignore session disconnected
                    logger.error(e) { "WebSocket processing error" }
                }
            }.doFinally {
                upstream?.let { upstream ->
                    mono {
                        logger.debug { "Close upstream authenticator connection ${upstream.id}" }
                        upstream.close(1000, "Completed")
                    }.publishOn(Schedulers.boundedElastic()).subscribe()
                }
            }
            .then()
    }
}