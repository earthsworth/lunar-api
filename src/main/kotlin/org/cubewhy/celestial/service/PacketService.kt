package org.cubewhy.celestial.service

import com.lunarclient.authenticator.v1.AuthSuccessMessage
import com.lunarclient.websocket.handshake.v1.Handshake
import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage as AuthenticatorServerboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage as AssetsServerboundWebsocketMessage
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType

interface PacketService {
    suspend fun process(
        message: AssetsServerboundWebsocketMessage,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processAuthorize(message: AuthenticatorServerboundWebsocketMessage): AuthSuccessMessage?
    suspend fun processHandshake(message: Handshake, session: WebSocketSession): User?
    suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User)
}