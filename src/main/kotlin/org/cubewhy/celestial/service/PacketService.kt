package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import com.lunarclient.websocket.handshake.v1.WebsocketHandshakeV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import org.springframework.web.reactive.socket.WebSocketSession

interface PacketService {
    suspend fun process(message: WebsocketProtocolV1.ServerboundWebSocketMessage, session: WebSocketSession): GeneratedMessage?
    suspend fun processAuthorize(message: LunarclientAuthenticatorV1.ServerboundWebSocketMessage): LunarclientAuthenticatorV1.AuthSuccessMessage?
    suspend fun processHandshake(message: WebsocketHandshakeV1.Handshake, session: WebSocketSession)
}