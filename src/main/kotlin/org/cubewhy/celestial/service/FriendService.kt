package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.friend.v1.*
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.springframework.web.reactive.socket.WebSocketSession

interface FriendService : PacketProcessor {
    suspend fun processLogin(
        user: User,
        session: WebSocketSession
    ): WebsocketResponse

    suspend fun processAddFriendRequest(
        message: SendFriendRequestRequest,
        user: User
    ): GeneratedMessage

    suspend fun hasFriend(user: User, target: User): Boolean
    suspend fun processToggleFriendRequests(message: ToggleFriendRequestsRequest, user: User): GeneratedMessage
    suspend fun processBroadcastStatusChange(
        message: BroadcastStatusChangeRequest,
        user: User
    ): GeneratedMessage

    suspend fun processAcceptFriendRequestRequest(
        message: AcceptFriendRequestRequest,
        user: User
    ): GeneratedMessage

    suspend fun processRemoveFriendPinRequest(
        message: RemoveFriendPinRequest,
        user: User
    ): GeneratedMessage

    suspend fun processAddFriendPinRequest(message: AddFriendPinRequest, user: User): GeneratedMessage
    suspend fun processDenyFriendRequest(
        message: DenyFriendRequestRequest,
        user: User
    ): GeneratedMessage

    suspend fun processCancelFriendRequest(
        message: CancelFriendRequestRequest,
        user: User
    ): GeneratedMessage

    suspend fun processRemoveFriendRequest(message: RemoveFriendRequest, user: User): GeneratedMessage
    suspend fun userOffline(user: User)
}