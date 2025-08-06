package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.handshake.v1.Handshake
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection
import reactor.core.publisher.SignalType
import com.lunarclient.authenticator.v1.ServerboundWebSocketMessage as AuthenticatorServerboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage as AssetsServerboundWebsocketMessage

interface PacketService {
    suspend fun process(
        message: AssetsServerboundWebsocketMessage,
        connection: ClientConnection<*>
    ): RpcResponse

    suspend fun processAuthorize(
        connection: ClientConnection<*>,
        message: AuthenticatorServerboundWebsocketMessage,
        upstreamAuthenticatorConnection: ClientConnection<*>?
    ): GeneratedMessage?

    suspend fun processHandshake(message: Handshake, connection: ClientConnection<*>): User?
    suspend fun processDisconnect(signalType: SignalType, connection: ClientConnection<*>, user: User)
}