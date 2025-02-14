package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.LunarclientCommonV1
import com.lunarclient.websocket.friend.v1.WebsocketFriendV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.event.UserOfflineEvent
import org.cubewhy.celestial.repository.FriendRepository
import org.cubewhy.celestial.repository.FriendRequestRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Instant


@Service
class FriendServiceImpl(
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val sessionService: SessionService,
    private val scope: CoroutineScope
) : FriendService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Value("\${lunar.friend.bot.enabled}")
    var botState = true

    @Value("\${lunar.friend.bot.username}")
    var botUsername = "lunar_cn"

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "Login" -> this.processLogin(user, session)
            "SendFriendRequest" -> this.processAddFriendRequest(
                WebsocketFriendV1.SendFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()

        }
    }

    /**
     * process friend login request
     */
    override suspend fun processLogin(
        user: User,
        session: WebSocketSession
    ): WebsocketResponse {
        val friends = findFriends(user)
        val botFriend = if (botState) buildBotFriend(user) else null
        // update friend status
        val events = mutableListOf<GeneratedMessage>()
        if (botFriend != null) {
            // push bot status
            events.add(this.buildOnlineFriendStatusPush(botFriend, true))
        }
        events.addAll(friends.map { friend ->
            // push friend status
            this.buildOnlineFriendStatusPush(friend)
        })
        return WebsocketResponse.create(WebsocketFriendV1.LoginResponse.newBuilder().apply {
            this.allowFriendRequests = user.allowFriendRequests
            botFriend?.let { this.addOfflineFriends(it) }
            this.addAllOfflineFriends(friends)
        }.build(), events)
    }

    private suspend fun buildOnlineFriendStatusPush(friend: WebsocketFriendV1.OfflineFriend, bot: Boolean = false): WebsocketFriendV1.FriendStatusPush =
        WebsocketFriendV1.FriendStatusPush.newBuilder().apply {
            this.onlineFriend = this@FriendServiceImpl.buildOnlineFriend(friend, bot)
        }.build()

    private suspend fun buildOnlineFriend(friend: WebsocketFriendV1.OfflineFriend, bot: Boolean = false): WebsocketFriendV1.OnlineFriend {
        val friendUuid = friend.player.uuid.toUUIDString()
        return WebsocketFriendV1.OnlineFriend.newBuilder().apply {
            this.player = friend.player
            this.plusColor = friend.plusColor
            this.friendsSince = friend.friendsSince
            this.logoColor = friend.logoColor
            // todo modpack
            sessionService.getMinecraftVersion(friendUuid)?.let {
                this.minecraftVersion = LunarclientCommonV1.MinecraftVersion.newBuilder().setEnum(it).build()
            }
            sessionService.getLocation(friendUuid)?.let {
                this.location = it
            }
            if (bot) {
                this.location = this@FriendServiceImpl.buildBotLocation()
            }
        }.build()
    }

    private fun buildBotLocation() = LunarclientCommonV1.Location.newBuilder().apply {
        this.publicServer = LunarclientCommonV1.PublicServer.newBuilder().apply {
            this.serverMappingsId = "localhost"
            this.name = "lunarclient.top"
        }.build()
    }.build()

    /**
     * Process friend add request
     * */
    override suspend fun processAddFriendRequest(
        message: WebsocketFriendV1.SendFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val targetUsername = message.targetUsername

        logger.info { "User ${user.username} send friend request to $targetUsername" }

        if (user.username.equals(targetUsername, ignoreCase = true)) {
            return buildResponse(
                user,
                WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_IS_SENDER
            )
        }

        val targetUser = userRepository.findByUsernameIgnoreCase(targetUsername)
            .awaitFirstOrNull()



        when {
            targetUser == null -> return buildResponse(
                targetUsername,
                WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_INVALID_USERNAME
            )

            !targetUser.allowFriendRequests -> return buildResponse(
                targetUser,
                WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_FRIEND_REQUESTS_DISABLED
            )

            hasFriend(user, targetUser) -> return buildResponse(
                targetUser,
                WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_FRIENDS
            )

            hasInboundFriendRequests(user, targetUser) -> return buildResponse(
                targetUser,
                WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_HAVE_INBOUND_REQUEST
            )

            hasOutboundFriendRequests(user, targetUser) -> return buildResponse(
                targetUser,
                WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_HAVE_OUTBOUND_REQUEST
            )

            else -> {
                sendFriendRequest(user, targetUser)
                return buildResponse(
                    user,
                    WebsocketFriendV1.SendFriendRequestResponse_Status.SENDFRIENDREQUESTRESPONSE_STATUS_STATUS_OK
                )
            }
        }
    }

    private fun buildBotFriend(user: User): WebsocketFriendV1.OfflineFriend {
        return WebsocketFriendV1.OfflineFriend.newBuilder().apply {
            player = botUsername.toLunarClientPlayer()
            rankName = "Bot"
            friendsSince = user.createdAt.toProtobufType()
            lastVisibleOnline = Instant.now().toProtobufType()
        }.build()
    }

    private suspend fun findFriends(user: User): List<WebsocketFriendV1.OfflineFriend> {
        return friendRepository.findFriendRelations(user.id!!)
            .flatMap { friend ->
                userRepository.findById(friend.getTargetId(user))
                    .map { targetUser ->
                        buildOfflineFriend(
                            targetUser,
                            friend
                        )
                    }
            }
            .collectList()
            .awaitLast()
    }

    private fun buildOfflineFriend(
        friendUser: User,
        friend: Friend
    ): WebsocketFriendV1.OfflineFriend {
        return WebsocketFriendV1.OfflineFriend.newBuilder().apply {
            player = friendUser.toLunarClientPlayer()
            rankName = friendUser.role.rank
            friendsSince = friend.timestamp.toProtobufType()
            friendUser.lunarPlusColor?.let { plusColor = it.toLunarClientColor() }
            logoColor = friendUser.role.toLunarClientColor()
            isRadioPremium = friendUser.radioPremium
            lastVisibleOnline = friendUser.lastSeenAt.toProtobufType()
        }.build()
    }

    private suspend fun hasOutboundFriendRequests(user: User, targetUser: User): Boolean {
        return friendRequestRepository.existsBySenderIdAndRecipientId(user.id!!, targetUser.id!!).awaitFirst()
    }

    private suspend fun hasInboundFriendRequests(user: User, targetUser: User): Boolean {
        return friendRequestRepository.existsBySenderIdAndRecipientId(targetUser.id!!, user.id!!).awaitFirst()
    }

    override suspend fun hasFriend(user: User, target: User): Boolean {
        return friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirstOrNull() != null
    }

    private suspend fun sendFriendRequest(user: User, target: User) {
        friendRequestRepository.save(FriendRequest(null, user.id!!, target.id!!, Instant.now())).awaitFirst()
        // send notification to target
        sessionService.getSession(target)?.let { session ->
            session.pushEvent(WebsocketFriendV1.FriendRequestReceivedPush.newBuilder().apply {
                sender = user.toLunarClientPlayer()
                senderLogoColor = user.role.toLunarClientColor()
                user.lunarPlusColor?.let { color ->
                    senderPlusColor = color.toLunarClientColor()
                }
                senderRankName = user.role.rank

            }.build())
        }
    }

    /**
     * Build SendFriendRequestResponse
     *
     * @param targetUser the player
     * @param status     Status
     * @return Friend response
     */
    private fun buildResponse(
        targetUser: User,
        status: WebsocketFriendV1.SendFriendRequestResponse_Status
    ): WebsocketFriendV1.SendFriendRequestResponse {
        return WebsocketFriendV1.SendFriendRequestResponse.newBuilder()
            .setTarget(targetUser.toLunarClientPlayer())
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
    private fun buildResponse(
        username: String,
        status: WebsocketFriendV1.SendFriendRequestResponse_Status
    ): WebsocketFriendV1.SendFriendRequestResponse {
        return WebsocketFriendV1.SendFriendRequestResponse.newBuilder()
            .setTarget(username.toLunarClientPlayer())
            .setStatus(status)
            .build()
    }

    @EventListener
    fun onUserOffline(event: UserOfflineEvent) {
        val user = event.user
        scope.launch {
            friendRepository.findFriendRelations(user.id!!)
                .flatMap { friend ->
                    userRepository.findById(friend.getTargetId(user)).flatMap { friendUser ->
                        mono {
                            sessionService.getSession(friendUser)?.let { session ->
                                session.pushEvent(WebsocketFriendV1.FriendStatusPush.newBuilder().apply {
                                    offlineFriend = buildOfflineFriend(friendUser, friend) // went offline
                                }.build())
                            }
                        }
                    }
                }
                .collectList()
                .awaitLast()
        }
    }
}