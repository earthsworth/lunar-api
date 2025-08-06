package org.cubewhy.celestial.service

import com.lunarclient.websocket.handshake.v1.Handshake
import org.cubewhy.celestial.entity.UpstreamAuthResponse
import org.cubewhy.celestial.protocol.ClientConnection
import com.lunarclient.websocket.protocol.v1.ClientboundWebSocketMessage as RpcClientboundWebSocketMessage


interface ExtendService {
    suspend fun openAuthConnection(): ClientConnection<*>
    suspend fun openRpcConnection(
        baseHandshake: Handshake,
        upstreamToken: String,
        handler: suspend (RpcClientboundWebSocketMessage) -> Unit
    ): ClientConnection<*>

    suspend fun awaitForAuthResponse(
        connection: ClientConnection<*>,
        beforeAwait: suspend () -> Unit = {}
    ): UpstreamAuthResponse?

    suspend fun awaitForNextMessage(connection: ClientConnection<*>, beforeAwait: suspend () -> Unit = {}): ByteArray?
}