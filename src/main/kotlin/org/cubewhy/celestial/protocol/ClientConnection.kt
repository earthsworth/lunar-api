package org.cubewhy.celestial.protocol

import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.UuidAndUsername
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.util.wrapCommon
import org.cubewhy.celestial.util.wrapPush
import java.security.KeyPair

/**
 * Abstract base class representing a client connection.
 *
 * @param T the type of the native underlying connection
 */
abstract class ClientConnection<T> {
    /**
     * The underlying native connection instance.
     */
    abstract val nativeConnection: T

    /**
     * Metadata associated with this connection, such as encryption keys and session information.
     */
    val metadata: ConnectionMetadata = ConnectionMetadata()

    abstract val id: String

    /**
     * Indicates whether the connection is currently open.
     */
    abstract val isOpen: Boolean

    /**
     * Sends a single binary message over the connection.
     *
     * @param message the message payload as a byte array
     */
    abstract suspend fun send(payload: ByteArray)

    open suspend fun send(message: GeneratedMessage) {
        this.send(message.toByteArray())
    }

    /**
     * Sends multiple binary messages over the connection.
     * The default implementation sends messages sequentially one by one.
     *
     * @param messages a list of message payloads
     */
    open suspend fun sendBulk(messages: List<GeneratedMessage>) {
        messages.forEach { this.send(it) }
    }

    /**
     * Closes the connection with the given code and an optional reason.
     *
     * @param code the close status code
     * @param reason the reason for closing, or null if none
     */
    abstract suspend fun close(code: Int, reason: String?)

    /**
     * Sends a [RpcResponse] over the connection, encrypting if needed.
     *
     * The method first checks if the connection is open.
     * Then it builds the main protobuf response message, followed by any included event messages.
     * All messages are sent as a batch.
     *
     * @param response the websocket response object to send
     */
    suspend fun sendResponse(response: RpcResponse) {
        if (!this.isOpen) {
            // session closed
            return
        }
        val responsePayload = response.response?.wrapCommon(response.requestId!!)

        val messages = mutableListOf<GeneratedMessage>()
        // add response payload
        responsePayload?.let { messages.add(it) }

        // build pushes
        // note: shared events are send in the handler, not in the connection layer
        messages.addAll(response.pushes.filter { !it.broadcast }
            .map { it.payload.wrapPush() })
        // send bulk
        this.sendBulk(messages)
    }

    /**
     * Sends a single event message with encryption if applicable.
     *
     * This method serializes the given protobuf [event],
     * and sends the resulting message over the connection using encryption when available.
     *
     * @param event the protobuf event message to send
     */
    suspend fun sendPush(event: GeneratedMessage) {
        this.send(event.wrapPush())
    }

    data class ConnectionMetadata(
        // assets
        var userId: String? = null,
        var multiplayerUuids: MutableList<String> = mutableListOf(),
        var language: String? = null,

        // authenticator
        var identity: UuidAndUsername? = null,
        var needVerify: Boolean = false,
        var keypair: KeyPair? = null,
        var randomBytes: ByteArray? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConnectionMetadata

            if (needVerify != other.needVerify) return false
            if (userId != other.userId) return false
            if (identity != other.identity) return false
            if (keypair != other.keypair) return false
            if (!randomBytes.contentEquals(other.randomBytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = needVerify.hashCode()
            result = 31 * result + (userId?.hashCode() ?: 0)
            result = 31 * result + (identity?.hashCode() ?: 0)
            result = 31 * result + (keypair?.hashCode() ?: 0)
            result = 31 * result + (randomBytes?.contentHashCode() ?: 0)
            return result
        }
    }
}