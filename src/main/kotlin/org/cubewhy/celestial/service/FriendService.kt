package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface FriendService : PacketProcessor {
    suspend fun processLogin(
        user: User,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processAddFriendRequest(
        message: WebsocketFriendV1.SendFriendRequestRequest,
        user: User
    ): GeneratedMessage

    suspend fun hasFriend(user: User, target: User): Boolean
    suspend fun processToggleFriendRequests(message: WebsocketFriendV1.ToggleFriendRequestsRequest, user: User): GeneratedMessage
}