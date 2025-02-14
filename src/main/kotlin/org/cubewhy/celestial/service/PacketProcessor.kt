package org.cubewhy.celestial.service

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface PacketProcessor {
    /**
     * Process packet
     *
     * @param method request method
     * @param payload payload
     * @param session websocket session
     * @param user issuer
     * @return response message
     * */
    suspend fun process(method: String, payload: ByteString, session: WebSocketSession, user: User): WebsocketResponse
}