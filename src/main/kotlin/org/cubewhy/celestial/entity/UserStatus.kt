package org.cubewhy.celestial.entity

import com.lunarclient.websocket.friend.v1.OnlineFriendStatus

enum class UserStatus(val protoType: OnlineFriendStatus) {
    ONLINE(OnlineFriendStatus.ONLINE_FRIEND_STATUS_ONLINE),
    AWAY(OnlineFriendStatus.ONLINE_FRIEND_STATUS_AWAY),
    BUSY(OnlineFriendStatus.ONLINE_FRIEND_STATUS_BUSY),
    INVISIBLE(OnlineFriendStatus.ONLINE_FRIEND_STATUS_INVISIBLE);

    companion object {
        fun resolve(proto: OnlineFriendStatus): UserStatus {
            return when (proto) {
                OnlineFriendStatus.ONLINE_FRIEND_STATUS_ONLINE -> ONLINE
                OnlineFriendStatus.ONLINE_FRIEND_STATUS_AWAY -> AWAY
                OnlineFriendStatus.ONLINE_FRIEND_STATUS_BUSY -> BUSY
                OnlineFriendStatus.ONLINE_FRIEND_STATUS_INVISIBLE -> INVISIBLE
                else -> ONLINE // default to online
            }
        }
    }
}