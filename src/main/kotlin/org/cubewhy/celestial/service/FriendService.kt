package org.cubewhy.celestial.service

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface FriendService {
    suspend fun processFriendRequest(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage?

    suspend fun processLogin(user: User): GeneratedMessage?
    suspend fun processAddFriendRequest(
        message: WebsocketFriendV1.SendFriendRequestRequest,
        user: User
    ): GeneratedMessage
}