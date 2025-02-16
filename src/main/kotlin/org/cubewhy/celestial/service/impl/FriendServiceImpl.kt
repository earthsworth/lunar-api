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

    @Value("\${lunar.friend.max}")
    var maxFriend = 50

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

            "ToggleFriendRequests" -> this.processToggleFriendRequests(
                WebsocketFriendV1.ToggleFriendRequestsRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "BroadcastStatusChange" -> this.processBroadcastStatusChange(
                WebsocketFriendV1.BroadcastStatusChangeRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "AcceptFriendRequest" -> this.processAcceptFriendRequestRequest(
                WebsocketFriendV1.AcceptFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "DenyFriendRequest" -> this.processDenyFriendRequest(
                WebsocketFriendV1.DenyFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "CancelFriendRequest" -> this.processCancelFriendRequest(
                WebsocketFriendV1.CancelFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "RemoveFriendPinRequest" -> this.processRemoveFriendPinRequest(
                WebsocketFriendV1.RemoveFriendPinRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "AddFriendPinRequest" -> this.processAddFriendPinRequest(
                WebsocketFriendV1.AddFriendPinRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()

        }
    }

    override suspend fun processCancelFriendRequest(
        message: WebsocketFriendV1.CancelFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val target = userRepository.findByUuid(message.targetUuid.toUUIDString()).awaitFirst()
        friendRequestRepository.deleteBySenderIdAndRecipientId(user.id!!, target.id!!).awaitFirst()
        return WebsocketFriendV1.CancelFriendRequestResponse.getDefaultInstance()
    }

    override suspend fun processDenyFriendRequest(
        message: WebsocketFriendV1.DenyFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val target = userRepository.findByUuid(message.senderUuid.toUUIDString()).awaitFirst()
        friendRequestRepository.deleteBySenderIdAndRecipientId(target.id!!, user.id!!).awaitFirst()
        val targetSession = sessionService.getSession(target)
        targetSession?.pushEvent(WebsocketFriendV1.FriendRequestDeniedPush.newBuilder().apply {
            denierUuid = user.uuid.toLunarClientUUID()
        }.build())
        return WebsocketFriendV1.DenyFriendRequestResponse.getDefaultInstance()
    }

    override suspend fun processRemoveFriendPinRequest(
        message: WebsocketFriendV1.RemoveFriendPinRequest,
        user: User
    ): GeneratedMessage {
        val builder = WebsocketFriendV1.RemoveFriendPinResponse.newBuilder()
        val target = userRepository.findByUuid(message.targetUuid.toUUIDString()).awaitFirst()
        if (target == null) {
            builder.status =
                WebsocketFriendV1.RemoveFriendPinResponse_Status.REMOVEFRIENDPINRESPONSE_STATUS_STATUS_TARGET_NOT_FOUND
        } else if (friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirst() == null) {
            builder.status =
                WebsocketFriendV1.RemoveFriendPinResponse_Status.REMOVEFRIENDPINRESPONSE_STATUS_STATUS_TARGET_IS_NOT_FRIEND
        } else if (!target.pinFriends.contains(user.id)) {
            builder.status =
                WebsocketFriendV1.RemoveFriendPinResponse_Status.REMOVEFRIENDPINRESPONSE_STATUS_STATUS_FRIEND_NOT_PINNED
        } else {
            builder.status = WebsocketFriendV1.RemoveFriendPinResponse_Status.REMOVEFRIENDPINRESPONSE_STATUS_STATUS_OK
        }
        return builder.build()
    }

    override suspend fun processAddFriendPinRequest(
        message: WebsocketFriendV1.AddFriendPinRequest,
        user: User
    ): GeneratedMessage {
        val builder = WebsocketFriendV1.AddFriendPinResponse.newBuilder()
        val target = userRepository.findByUuid(message.targetUuid.toUUIDString()).awaitFirst()
        if (target == null) {
            builder.status =
                WebsocketFriendV1.AddFriendPinResponse_Status.ADDFRIENDPINRESPONSE_STATUS_STATUS_TARGET_NOT_FOUND
        } else if (friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirst() == null) {
            builder.status =
                WebsocketFriendV1.AddFriendPinResponse_Status.ADDFRIENDPINRESPONSE_STATUS_STATUS_TARGET_IS_NOT_FRIEND
        } else if (target.pinFriends.contains(user.id)) {
            builder.status =
                WebsocketFriendV1.AddFriendPinResponse_Status.ADDFRIENDPINRESPONSE_STATUS_STATUS_FRIEND_ALREADY_PINNED
        } else {
            builder.status = WebsocketFriendV1.AddFriendPinResponse_Status.ADDFRIENDPINRESPONSE_STATUS_STATUS_OK
        }
        return builder.build()
    }

    override suspend fun processAcceptFriendRequestRequest(
        message: WebsocketFriendV1.AcceptFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val builder = WebsocketFriendV1.AcceptFriendRequestResponse.newBuilder()
        val target = userRepository.findByUuid(message.senderUuid.toUUIDString()).awaitFirst()
        val targetSession = sessionService.getSession(target)
        if (friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirst() != null) {
            builder.status =
                WebsocketFriendV1.AcceptFriendRequestResponse_Status.ACCEPTFRIENDREQUESTRESPONSE_STATUS_STATUS_ALREADY_FRIENDS
            return builder.build()
        }
        if (friendRequestRepository.existsBySenderIdAndRecipientId(target.id, user.id).awaitFirst()) {
            val userFriend = friendRepository.countByUser1(user.id).awaitFirst()
            val targetFriend = friendRepository.countByUser1(target.id).awaitFirst()
            if (userFriend >= maxFriend) {
                builder.status =
                    WebsocketFriendV1.AcceptFriendRequestResponse_Status.ACCEPTFRIENDREQUESTRESPONSE_STATUS_STATUS_YOUR_FRIEND_LIST_FULL
            } else if (targetFriend >= maxFriend) {
                builder.status =
                    WebsocketFriendV1.AcceptFriendRequestResponse_Status.ACCEPTFRIENDREQUESTRESPONSE_STATUS_STATUS_TARGET_FRIEND_LIST_FULL
            } else {
                builder.status =
                    WebsocketFriendV1.AcceptFriendRequestResponse_Status.ACCEPTFRIENDREQUESTRESPONSE_STATUS_STATUS_OK
                builder.offlineFriend =
                    buildOfflineFriend(target, friendRepository.save(Friend(null, target.id, user.id)).awaitFirst())
                targetSession?.pushEvent(WebsocketFriendV1.FriendRequestAcceptedPush.newBuilder().apply {
                    newFriendUuid = user.uuid.toLunarClientUUID()
                    newFriend = user.toLunarClientPlayer()
                }.build())
            }
        } else {
            builder.status =
                WebsocketFriendV1.AcceptFriendRequestResponse_Status.ACCEPTFRIENDREQUESTRESPONSE_STATUS_STATUS_FRIEND_REQUEST_NOT_FOUND
        }
        return builder.build()
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
            events.add(this.buildOnlineFriendStatusPush(botFriend, bot = true))
        }
        events.addAll(friends.mapNotNull { friend ->
            // push friend status
            if (friend.friendUser.status != UserStatus.INVISIBLE) {
                this.buildOnlineFriendStatusPush(friend.lunarType, friend.friendUser)
            } else null
        })

        val incomingRequests = findAllIncomingFriendRequests(user)
        val outgoingRequests = findAllOutgoingFriendRequests(user)
        return WebsocketResponse.create(WebsocketFriendV1.LoginResponse.newBuilder().apply {
            this.allowFriendRequests = user.allowFriendRequests
            botFriend?.let { this.addOfflineFriends(it) }
            this.addAllOfflineFriends(friends.map { it.lunarType })
            this.currentStatus = user.status.protoType
            // todo friend requests
            this.addAllInboundFriendRequests(incomingRequests.map { request ->
                userRepository.findById(request.senderId).awaitFirst().toLunarClientPlayer()
            })
            this.addAllInboundFriendAddRequests(incomingRequests.map { this@FriendServiceImpl.buildFriendRequest(it) })
            this.addAllOutboundFriendRequests(outgoingRequests.map { request ->
                userRepository.findById(request.senderId).awaitFirst().toLunarClientPlayer()
            })
            this.addAllOutboundFriendAddRequests(outgoingRequests.map { this@FriendServiceImpl.buildFriendRequest(it) })
        }.build(), events)
    }

    private suspend fun buildFriendRequest(request: FriendRequest): WebsocketFriendV1.FriendRequest {
        val recipient = userRepository.findById(request.recipientId).awaitFirst()
        return WebsocketFriendV1.FriendRequest.newBuilder().apply {
            this.player = recipient.toLunarClientPlayer()
            this.sentAt = request.timestamp.toProtobufType()
            this.playerLogoColor = recipient.role.toLunarClientColor()
            this.playerRankName = recipient.role.rank
            if (recipient.cosmetic.lunarPlusState) {
                this.playerPlusColor = recipient.cosmetic.lunarPlusColor.toLunarClientColor()
            }
        }.build()
    }

    private suspend fun findAllIncomingFriendRequests(user: User) =
        friendRequestRepository.findAllByRecipientId(user.id!!)
            .collectList()
            .awaitLast()

    private suspend fun findAllOutgoingFriendRequests(user: User) =
        friendRequestRepository.findAllBySenderId(user.id!!)
            .collectList()
            .awaitLast()

    private suspend fun buildOnlineFriendStatusPush(
        friend: WebsocketFriendV1.OfflineFriend,
        friendUser: User? = null,
        bot: Boolean = false
    ): WebsocketFriendV1.FriendStatusPush =
        WebsocketFriendV1.FriendStatusPush.newBuilder().apply {
            this.onlineFriend = this@FriendServiceImpl.buildOnlineFriend(friend, friendUser, bot)
        }.build()

    private suspend fun buildOnlineFriend(
        friend: WebsocketFriendV1.OfflineFriend,
        friendUser: User? = null,
        bot: Boolean = false
    ): WebsocketFriendV1.OnlineFriend {
        val friendUuid = friend.player.uuid.toUUIDString()
        return WebsocketFriendV1.OnlineFriend.newBuilder().apply {
            this.player = friend.player
            this.plusColor = friend.plusColor
            this.friendsSince = friend.friendsSince
            this.logoColor = friend.logoColor
            friendUser?.let {
                this.status = it.status.protoType
            }
            if (bot) {
                this.status = UserStatus.ONLINE.protoType
            }
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
            this.name = "type .help for help"
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

    override suspend fun processToggleFriendRequests(
        message: WebsocketFriendV1.ToggleFriendRequestsRequest,
        user: User
    ): GeneratedMessage {
        logger.info { "User ${if (message.allowFriendRequests) "enabled" else "disabled"} incoming friend requests" }
        user.allowFriendRequests = message.allowFriendRequests
        // save user
        userRepository.save(user).awaitFirst()
        return WebsocketFriendV1.ToggleFriendRequestsResponse.getDefaultInstance()
    }

    override suspend fun processBroadcastStatusChange(
        message: WebsocketFriendV1.BroadcastStatusChangeRequest,
        user: User
    ): GeneratedMessage {
        // save status
        user.status = UserStatus.resolve(message.newStatus)
        userRepository.save(user).awaitFirst()
        logger.info { "User ${user.username} updated its status ${user.status}" }
        // broadcast to other users
        this.findFriends(user).forEach { target ->
            // push event
            sessionService.getSession(target.friendUser)
                ?.pushEvent(this.buildOnlineFriendStatusPush(target.lunarType, user))
        }
        return WebsocketFriendV1.BroadcastStatusChangeResponse.getDefaultInstance()
    }

    private fun buildBotFriend(user: User): WebsocketFriendV1.OfflineFriend {
        return WebsocketFriendV1.OfflineFriend.newBuilder().apply {
            player = botUsername.toLunarClientPlayer(bot = true)
            rankName = "Bot"
            friendsSince = user.createdAt.toProtobufType()
            lastVisibleOnline = Instant.now().toProtobufType()
        }.build()
    }

    private suspend fun findFriends(user: User): List<InternalFriendDTO> {
        return friendRepository.findFriendRelations(user.id!!)
            .flatMap { friend ->
                userRepository.findById(friend.getTargetId(user))
                    .map { targetUser ->
                        InternalFriendDTO(
                            lunarType = buildOfflineFriend(
                                targetUser,
                                friend
                            ),
                            friendUser = targetUser
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
            if (friendUser.cosmetic.lunarPlusState) {
                plusColor = friendUser.cosmetic.lunarPlusColor.toLunarClientColor()
            }
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
                if (user.cosmetic.lunarPlusState) {
                    senderPlusColor = user.cosmetic.lunarPlusColor.toLunarClientColor()
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

private data class InternalFriendDTO(
    val lunarType: WebsocketFriendV1.OfflineFriend,
    val friendUser: User
)