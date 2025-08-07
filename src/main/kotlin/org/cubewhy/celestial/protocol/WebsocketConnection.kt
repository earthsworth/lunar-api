package org.cubewhy.celestial.protocol

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono


class WebsocketConnection(override val nativeConnection: WebSocketSession) : ClientConnection<WebSocketSession>() {
    override val id: String
        get() = this.nativeConnection.id

    override val isOpen: Boolean
        get() = this.nativeConnection.isOpen

    override suspend fun send(payload: ByteArray) {
        if (this.isOpen) {
            this.nativeConnection.send(this.nativeConnection.binaryMessage { it.wrap(payload) }.toMono())
                .awaitFirstOrNull()
        }
    }

    override suspend fun close(code: Int, reason: String?) {
        if (this.isOpen) {
            this.nativeConnection.close(CloseStatus.create(code, reason)).awaitFirstOrNull()
        }
    }
}