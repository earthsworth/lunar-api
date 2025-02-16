package org.cubewhy.celestial.entity

import com.lunarclient.websocket.friend.v1.WebsocketFriendV1

enum class UserStatus(val protoType: WebsocketFriendV1.OnlineFriendStatus) {
    ONLINE(WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_ONLINE),
    AWAY(WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_AWAY),
    BUSY(WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_BUSY),
    INVISIBLE(WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_INVISIBLE);

    companion object {
        fun resolve(proto: WebsocketFriendV1.OnlineFriendStatus): UserStatus {
            return when (proto) {
                WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_ONLINE -> ONLINE
                WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_AWAY -> AWAY
                WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_BUSY -> BUSY
                WebsocketFriendV1.OnlineFriendStatus.ONLINEFRIENDSTATUS_ONLINE_FRIEND_STATUS_INVISIBLE -> INVISIBLE
                else -> ONLINE // default to online
            }
        }
    }
}