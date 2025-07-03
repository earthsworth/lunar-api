package org.cubewhy.celestial.service

import com.google.protobuf.ByteString
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface PacketProcessor {
    val serviceName: String

    /**
     * Process packet
     *
     * @param method request method
     * @param payload payload
     * @param connection websocket session
     * @param user issuer
     * @return response message
     * */
    suspend fun process(method: String, payload: ByteString, connection: ClientConnection<*>, user: User): RpcResponse
}