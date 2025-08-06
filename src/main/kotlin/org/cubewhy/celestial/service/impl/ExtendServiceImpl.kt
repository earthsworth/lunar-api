package org.cubewhy.celestial.service.impl

import com.lunarclient.authenticator.v1.ClientboundWebSocketMessage
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

@Service
class ExtendServiceImpl(
    private val lunarProp: LunarProperties,
    private val websocketClient: ReactorNettyWebSocketClient
) : ExtendService {
    companion object {
        private val logger = KotlinLogging.logger {}

        val authConnections: MutableMap<String, ClientConnection<*>> = mutableMapOf()
        val authPromisesMap: MutableMap<String, MutableList<CompletableDeferred<ClientboundWebSocketMessage>>> =
            mutableMapOf()
    }

    fun removeAuthConnection(connId: String) {
        logger.info { "Upstream auth session $connId closed" }
        // clean promise
        authPromisesMap.remove(connId)
        // remove from connections map
        authConnections.remove(connId)
    }

    fun addAuthConnection(conn: ClientConnection<*>) {
        logger.info { "Upstream auth session ${conn.id} opened" }
        authConnections[conn.id] = conn
    }

    suspend fun handleAuthMessage(connection: ClientConnection<*>, message: ClientboundWebSocketMessage) {
        logger.info {
            val msgType =
                if (message.hasAuthSuccess()) "auth success" else if (message.hasEncryptionRequest()) "join server request" else "<unknown>"
            "Upstream auth session ${connection.id} received $msgType message"
        }
        // find promises
        authPromisesMap[connection.id]?.forEach { promise ->
            // complete promise
            promise.complete(message)
        }
    }

    override suspend fun openAuthConnection(): ClientConnection<*> {
        val promise = CompletableDeferred<ClientConnection<*>>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                websocketClient.execute(URI.create(lunarProp.upstream.auth)) { session ->
                    logger.info { "Handle upstream auth session" }
                    val conn = WebsocketConnection(session)
                    promise.complete(conn)

                    session.receive()
                        .flatMap { message ->
                            // parse message
                            val payload = ClientboundWebSocketMessage.parseFrom(message.payload.asInputStream())
                            mono { handleAuthMessage(conn, payload) }
                        }
                        .doFinally {
                            removeAuthConnection(conn.id)
                        }.then()
                }.awaitFirstOrNull()
            } catch (e: Exception) {
                logger.error(e) { "Failed to connect to upstream" }
                promise.completeExceptionally(e)
            }
        }

        // wait for connection and add connection to cache
        val connection = promise.await()
        addAuthConnection(connection)
        return connection
    }

    override suspend fun awaitForAuthResponse(connection: ClientConnection<*>): UpstreamAuthResponse? {
        if (!(connection.isOpen)) return null // connection closed
        // create promise list if not found
        if (authPromisesMap[connection.id] == null) {
            authPromisesMap[connection.id] = mutableListOf()
        }
        // find promise list
        val promises = authPromisesMap[connection.id]!!
        // create promise
        val promise = CompletableDeferred<ClientboundWebSocketMessage>()
        promises.add(promise)

        val response = promise.await() // wait for response
        return UpstreamAuthResponse.from(response)
    }
}