package org.cubewhy.celestial.service

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface FriendService {
    suspend fun processFriendRequest(method: String, payload: ByteString, session: WebSocketSession, user: User): GeneratedMessage?
}