package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.handshake.v1.Handshake
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.SignalType
import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage as AuthenticatorServerboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage as AssetsServerboundWebsocketMessage

interface PacketService {
    suspend fun process(
        message: AssetsServerboundWebsocketMessage,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processAuthorize(
        session: WebSocketSession,
        message: AuthenticatorServerboundWebsocketMessage
    ): GeneratedMessage?
    suspend fun processHandshake(message: Handshake, session: WebSocketSession): User?
    suspend fun processDisconnect(signalType: SignalType, session: WebSocketSession, user: User)
}