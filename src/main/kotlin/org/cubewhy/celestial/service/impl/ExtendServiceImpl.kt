package org.cubewhy.celestial.service.impl

import com.lunarclient.websocket.handshake.v1.Handshake
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.UpstreamAuthResponse
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.protocol.WebsocketConnection
import org.cubewhy.celestial.service.ExtendService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.net.URI
import com.lunarclient.authenticator.v1.ClientboundWebSocketMessage as AuthClientboundWebSocketMessage
import com.lunarclient.websocket.protocol.v1.ClientboundWebSocketMessage as RpcClientboundWebSocketMessage

@Service
class ExtendServiceImpl(
    private val lunarProp: LunarProperties,
    private val websocketClient: ReactorNettyWebSocketClient
) : ExtendService {
    companion object {
        private val logger = KotlinLogging.logger {}

        val connections: MutableMap<String, ClientConnection<*>> = mutableMapOf()
        val promisesMap: MutableMap<String, MutableList<CompletableDeferred<ByteArray>>> =
            mutableMapOf()
    }

    fun removeConnection(connId: String) {
        // clean promise
        promisesMap.remove(connId)
        // remove from connections map
        connections.remove(connId)
    }

    fun addConnection(conn: ClientConnection<*>) {
        logger.info { "Upstream session ${conn.id} opened" }
        connections[conn.id] = conn
    }

    suspend fun handleMessageInternal(connection: ClientConnection<*>, message: ByteArray) {
        // find promises
        promisesMap[connection.id]?.forEach { promise ->
            // complete promise
            promise.complete(message)
        }
    }

    private suspend fun openConnection(
        url: String,
        handleMessage: suspend (ByteArray) -> Unit = {}
    ): ClientConnection<*> {
        val promise = CompletableDeferred<ClientConnection<*>>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                websocketClient.execute(URI.create(url)) { session ->
                    val conn = WebsocketConnection(session)
                    promise.complete(conn)

                    session.receive()
                        .flatMap { message ->
                            // parse message
                            val payload = message.payload.asInputStream().readAllBytes()
                            mono {
                                handleMessage(payload)
                                handleMessageInternal(conn, payload)
                            }
                        }
                        .doFinally {
                            CoroutineScope(Dispatchers.IO).launch {
                                val reason = conn.nativeConnection.closeStatus().awaitFirstOrNull()?.reason
                                logger.info { "Upstream session ${conn.id} closed (${reason})" }

                            }
                            removeConnection(conn.id)
                        }.then()
                }.awaitFirstOrNull()
            } catch (e: Exception) {
                // TODO: reconnect
                logger.error(e) { "Failed to connect to upstream" }
                promise.completeExceptionally(e)
            }
        }

        // wait for connection
        return promise.await().also { connection ->
            addConnection(connection)
        }
    }

    override suspend fun openAuthConnection(): ClientConnection<*> {
        // wait for connection open and add connection to cache
        val connection = openConnection(lunarProp.upstream.auth)
        return connection
    }

    override suspend fun openRpcConnection(
        baseHandshake: Handshake,
        upstreamToken: String,
        handler: suspend (RpcClientboundWebSocketMessage) -> Unit
    ): ClientConnection<*> {
        val connection = openConnection(lunarProp.upstream.rpc) { payload ->
            val message = RpcClientboundWebSocketMessage.parseFrom(payload)
            handler(message)
        }
        val identity = baseHandshake.identity.toBuilder().setAuthenticatorJwt(upstreamToken)
        val handshake = baseHandshake.toBuilder().setIdentity(identity).build()
        connection.send(handshake)
        return connection
    }

    override suspend fun awaitForNextMessage(
        connection: ClientConnection<*>,
        beforeAwait: suspend () -> Unit
    ): ByteArray? {
        if (!(connection.isOpen)) return null // connection closed
        // create promise list if not found
        if (promisesMap[connection.id] == null) {
            promisesMap[connection.id] = mutableListOf()
        }
        // find promise list
        val promises = promisesMap[connection.id]!!
        // create promise
        val promise = CompletableDeferred<ByteArray>()
        promises.add(promise)

        beforeAwait()

        return promise.await() // wait for response
    }

    override suspend fun awaitForAuthResponse(
        connection: ClientConnection<*>,
        beforeAwait: suspend () -> Unit
    ): UpstreamAuthResponse? {
        // parse payload
        val response = AuthClientboundWebSocketMessage.parseFrom(awaitForNextMessage(connection, beforeAwait))
        return UpstreamAuthResponse.from(response)
    }
}