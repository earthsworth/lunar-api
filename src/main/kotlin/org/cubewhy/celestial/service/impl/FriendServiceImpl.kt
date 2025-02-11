import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1.SendFriendRequestResponse
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1.SendFriendRequestResponse_Status
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.FriendRequest
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.FriendRepository
import org.cubewhy.celestial.repository.FriendRequestRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.util.calcTimestamp
import org.cubewhy.celestial.util.toLunarClientColor
import org.cubewhy.celestial.util.toUuidAndUsername
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Instant


@Service
class FriendServiceImpl(
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val friendRequestRepository: FriendRequestRepository,
) : FriendService {
    @Value("\${lunar.friend.bot.enabled}")
    var botState = true

    @Value("\${lunar.friend.bot.username}")
    var botUsername = "lunar_cn"

    override suspend fun processFriendRequest(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        when (method) {
            "Login" -> {
                return login(user)
            }

            "SendFriendRequest" -> {
                return addFriend(WebsocketFriendV1.SendFriendRequestRequest.parseFrom(payload), user)
            }

            else -> {
                return null
            }
        }
    }

    /**
     * process friend login request
     */

    private suspend fun login(
        user: User
    ): GeneratedMessage? {
        return WebsocketFriendV1.LoginResponse.newBuilder().apply {
            allowFriendRequests = user.allowFriendRequests
            if (botState) addOfflineFriends(buildBotFriend(user))
            addAllOfflineFriends(findFriends(user))

        }.build()
    }

    private suspend fun addFriend(
        message: WebsocketFriendV1.SendFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val targetUsername = message.targetUsername

        if (user.username.equals(targetUsername, ignoreCase = true)) {
            return buildResponse(
                user,
                SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_IS_SENDER
            )
        }

        val targetUser = userRepository.findByUsernameIgnoreCase(targetUsername)
            .awaitFirstOrNull()

        when {
            targetUser == null -> return buildResponse(
                user,
                SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_INVALID_USERNAME
            )

            !targetUser.allowFriendRequests -> return buildResponse(
                user,
                SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_FRIEND_REQUESTS_DISABLED
            )

            hasFriend(user, targetUser) -> return buildResponse(
                user,
                SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_FRIENDS
            )

            hasInboundFriendRequests(user, targetUser) -> return buildResponse(
                user,
                SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_HAVE_INBOUND_REQUEST
            )

            hasOutboundFriendRequests(user, targetUser) -> return buildResponse(
                user,
                SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_HAVE_OUTBOUND_REQUEST
            )

            else -> {
                sendFriendRequest(user, targetUser)
                return buildResponse(
                    user,
                    SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_OK
                )
            }
        }
    }

    private fun buildBotFriend(user: User): WebsocketFriendV1.OfflineFriend {
        return WebsocketFriendV1.OfflineFriend.newBuilder().apply {
            player = toUuidAndUsername(botUsername)
            rankName = "Bot"
            friendsSince = calcTimestamp(user.createdAt)
        }.build()
    }

    private suspend fun findFriends(user: User): List<WebsocketFriendV1.OfflineFriend> {
        return friendRepository.findFriendRelations(user.id!!)
            .flatMap { friend ->
                val targetId = if (user.id == friend.user1) friend.user2 else friend.user1
                userRepository.findById(targetId)
                    .map { targetUser ->
                        buildOfflineFriend(
                            targetUser,
                            friend.timestamp,
                            targetUser.role.color,
                            targetUser.radioPremium,
                            targetUser.lunarPlusColor,
                            targetUser.lastSeenAt
                        )
                    }
            }
            .collectList()
            .awaitFirst()
    }

    private fun buildOfflineFriend(
        user: User,
        since: Instant,
        targetLogoColor: Int,
        targetRadioPremium: Boolean,
        targetLunarPlusColor: Int?,
        targetLastSeenAt: Instant
    )
            : WebsocketFriendV1.OfflineFriend {
        return WebsocketFriendV1.OfflineFriend.newBuilder().apply {
            player = toUuidAndUsername(user.username)
            rankName = user.role.rank
            friendsSince = calcTimestamp(since)
            logoColor = targetLogoColor.toLunarClientColor()
            isRadioPremium = targetRadioPremium
            if (targetLunarPlusColor != null) plusColor = targetLunarPlusColor.toLunarClientColor()
            // TODO session.isOnline
        }.build()
    }

    private suspend fun hasOutboundFriendRequests(user: User, targetUser: User): Boolean {
        return friendRequestRepository.existsBySenderIdAndRecipientId(user.id!!, targetUser.id!!).awaitFirst()
    }

    private suspend fun hasInboundFriendRequests(user: User, targetUser: User): Boolean {
        return friendRequestRepository.existsBySenderIdAndRecipientId(targetUser.id!!, user.id!!).awaitFirst()
    }

    private suspend fun hasFriend(user: User, target:User) : Boolean{
        return friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirstOrNull()!=null
    }

    private suspend fun sendFriendRequest(user:User, target:User) {
        friendRequestRepository.save(FriendRequest(null, user.id!!, target.id!!, Instant.now())).awaitFirst()
        TODO("notification add friend request")
    }

    /**
     * Build SendFriendRequestResponse
     *
     * @param targetUser the player
     * @param status     Status
     * @return Friend response
     */
    private fun buildResponse(targetUser: User, status: SendFriendRequestResponse_Status): SendFriendRequestResponse {
        return SendFriendRequestResponse.newBuilder()
            .setTarget(toUuidAndUsername(targetUser))
            .setStatus(status)
            .build()
    }

    /**
     * Build SendFriendRequestResponse without UUID
     *
     * @param username player uuid
     * @param status   Status
     * @return Friend response
     */
    private fun buildResponse(username: String, status: SendFriendRequestResponse_Status): SendFriendRequestResponse {
        return SendFriendRequestResponse.newBuilder()
            .setTarget(toUuidAndUsername(username))
            .setStatus(status)
            .build()
    }
}