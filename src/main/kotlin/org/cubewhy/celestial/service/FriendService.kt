package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface FriendService : PacketProcessor {
    suspend fun processLogin(
        user: User,
        session: WebSocketSession
    ): GeneratedMessage?

    suspend fun processAddFriendRequest(
        message: WebsocketFriendV1.SendFriendRequestRequest,
        user: User
    ): GeneratedMessage
}