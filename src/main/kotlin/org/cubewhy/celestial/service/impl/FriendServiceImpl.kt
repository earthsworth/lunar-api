package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.common.v1.*
import com.lunarclient.websocket.friend.v1.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.FriendRequest
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
    var maxFriend: Long = -1

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "Login" -> this.processLogin(user, session)
            "SendFriendRequest" -> this.processAddFriendRequest(
                SendFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "ToggleFriendRequests" -> this.processToggleFriendRequests(
                ToggleFriendRequestsRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "BroadcastStatusChange" -> this.processBroadcastStatusChange(
                BroadcastStatusChangeRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "AcceptFriendRequest" -> this.processAcceptFriendRequestRequest(
                AcceptFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "DenyFriendRequest" -> this.processDenyFriendRequest(
                DenyFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "RemoveFriend" -> this.processRemoveFriendRequest(
                RemoveFriendRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "CancelFriendRequest" -> this.processCancelFriendRequest(
                CancelFriendRequestRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "RemoveFriendPinRequest" -> this.processRemoveFriendPinRequest(
                RemoveFriendPinRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            "AddFriendPinRequest" -> this.processAddFriendPinRequest(
                AddFriendPinRequest.parseFrom(payload),
                user
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()

        }
    }

    override suspend fun processCancelFriendRequest(
        message: CancelFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val target = userRepository.findByUuid(message.targetUuid.toUUIDString()).awaitFirst()
        friendRequestRepository.deleteBySenderIdAndRecipientId(user.id!!, target.id!!).awaitFirst()
        return CancelFriendRequestResponse.getDefaultInstance()
    }

    override suspend fun processDenyFriendRequest(
        message: DenyFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val target = userRepository.findByUuid(message.senderUuid.toUUIDString()).awaitFirst()
        friendRequestRepository.deleteBySenderIdAndRecipientId(target.id!!, user.id!!).awaitFirst()
        val targetSession = sessionService.getSession(target)
        // push event
        targetSession?.pushEvent(FriendRequestDeniedPush.newBuilder().apply {
            denierUuid = user.uuid.toLunarClientUUID()
        }.build())
        return DenyFriendRequestResponse.getDefaultInstance()
    }

    override suspend fun processRemoveFriendRequest(
        message: RemoveFriendRequest,
        user: User
    ): GeneratedMessage {
        val target = userRepository.findByUuid(message.friendUuid.toUUIDString()).awaitFirst()
        val relation = friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirstOrNull()
            ?: return RemoveFriendResponse.getDefaultInstance() // not friend
        val targetSession = sessionService.getSession(target)
        // remove relation
        logger.info { "Removed friend between ${user.username} and ${target.username}" }
        friendRepository.delete(relation).awaitFirstOrNull()
        // push event
        targetSession?.pushEvent(FriendRemovedYouPush.newBuilder().apply {
            this.friendUuid = target.uuid.toLunarClientUUID()
        }.build())
        return RemoveFriendResponse.getDefaultInstance()
    }

    override suspend fun userOffline(user: User) {
        // find friend relations
        val friends = friendRepository.findFriendRelations(user.id!!).collectList().awaitLast()
        // push events to friends
        friends.forEach { friend ->
            val friendId = if (user.id == friend.user1) friend.user2 else friend.user1
            val friendUser = userRepository.findById(friendId).awaitFirst()
            sessionService.getSession(friendId)?.let { session ->
                val offlineFriend = this.buildOfflineFriend(friendUser, friend)
                session.pushEvent(FriendStatusPush.newBuilder().apply {
                    this.offlineFriend = offlineFriend
                }.build())
            }
        }
    }

    override suspend fun processRemoveFriendPinRequest(
        message: RemoveFriendPinRequest,
        user: User
    ): GeneratedMessage {
        val builder = RemoveFriendPinResponse.newBuilder()
        val target = userRepository.findByUuid(message.targetUuid.toUUIDString()).awaitFirst()
        if (target == null) {
            builder.status =
                RemoveFriendPinResponse.Status.STATUS_TARGET_NOT_FOUND
        } else if (friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirst() == null) {
            builder.status =
                RemoveFriendPinResponse.Status.STATUS_TARGET_IS_NOT_FRIEND
        } else if (!target.pinFriends.contains(user.id)) {
            builder.status =
                RemoveFriendPinResponse.Status.STATUS_FRIEND_NOT_PINNED
        } else {
            builder.status = RemoveFriendPinResponse.Status.STATUS_OK
        }
        return builder.build()
    }

    override suspend fun processAddFriendPinRequest(
        message: AddFriendPinRequest,
        user: User
    ): GeneratedMessage {
        val builder = AddFriendPinResponse.newBuilder()
        val target = userRepository.findByUuid(message.targetUuid.toUUIDString()).awaitFirst()
        if (target == null) {
            builder.status =
                AddFriendPinResponse.Status.STATUS_TARGET_NOT_FOUND
        } else if (friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirst() == null) {
            builder.status =
                AddFriendPinResponse.Status.STATUS_TARGET_IS_NOT_FRIEND
        } else if (target.pinFriends.contains(user.id)) {
            builder.status =
                AddFriendPinResponse.Status.STATUS_FRIEND_ALREADY_PINNED
        } else {
            builder.status = AddFriendPinResponse.Status.STATUS_OK
        }
        return builder.build()
    }

    override suspend fun processAcceptFriendRequestRequest(
        message: AcceptFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val builder = AcceptFriendRequestResponse.newBuilder()
        val target = userRepository.findByUuid(message.senderUuid.toUUIDString()).awaitFirst()
        val targetSession = sessionService.getSession(target)
        if (friendRepository.findFriendRelation(user.id!!, target.id!!).awaitFirstOrNull() != null) {
            // delete friend request
            friendRequestRepository.deleteBySenderIdAndRecipientId(user.id, target.id).awaitFirst()
            builder.status =
                AcceptFriendRequestResponse.Status.STATUS_ALREADY_FRIENDS
            return builder.build()
        }
        if (friendRequestRepository.existsBySenderIdAndRecipientId(target.id, user.id).awaitFirst()) {
            val userFriendCount = friendRepository.countByUser1(user.id).awaitFirst()
            val targetFriendCount = friendRepository.countByUser1(target.id).awaitFirst()
            if (userFriendCount >= maxFriend) {
                builder.status =
                    AcceptFriendRequestResponse.Status.STATUS_YOUR_FRIEND_LIST_FULL
            } else if (targetFriendCount >= maxFriend) {
                builder.status =
                    AcceptFriendRequestResponse.Status.STATUS_TARGET_FRIEND_LIST_FULL
            } else {
                // save friend
                builder.status =
                    AcceptFriendRequestResponse.Status.STATUS_OK
                builder.offlineFriend =
                    buildOfflineFriend(target, friendRepository.save(Friend(null, target.id, user.id)).awaitFirst())
                // delete friend request
                friendRequestRepository.deleteBySenderIdAndRecipientId(user.id, target.id).awaitFirst()
                targetSession?.pushEvent(FriendRequestAcceptedPush.newBuilder().apply {
                    newFriendUuid = user.uuid.toLunarClientUUID()
                    newFriend = user.toLunarClientPlayer()
                }.build())
            }
        } else {
            builder.status =
                AcceptFriendRequestResponse.Status.STATUS_FRIEND_REQUEST_NOT_FOUND
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
            if (friend.friendUser.status != UserStatus.INVISIBLE && sessionService.getSession(friend.friendUser) != null) {
                this.buildOnlineFriendStatusPush(friend.lunarType, friend.friendUser)
            } else null
        })

        val incomingRequests = findAllIncomingFriendRequests(user)
        val outgoingRequests = findAllOutgoingFriendRequests(user)
        return WebsocketResponse.create(LoginResponse.newBuilder().apply {
            this.allowFriendRequests = user.allowFriendRequests
            botFriend?.let { this.addOfflineFriends(it) }
            this.addAllOfflineFriends(friends.map { it.lunarType })
            this.currentStatus = user.status.protoType
            // add friend requests
            this.addAllInboundFriendRequests(incomingRequests.map { request ->
                userRepository.findById(request.senderId).awaitFirst().toLunarClientPlayer()
            })
            this.addAllInboundFriendAddRequests(incomingRequests.map { this@FriendServiceImpl.buildFriendRequest(it) })
            this.addAllOutboundFriendRequests(outgoingRequests.map { request ->
                userRepository.findById(request.recipientId).awaitFirst().toLunarClientPlayer()
            })
            this.addAllOutboundFriendAddRequests(outgoingRequests.map { this@FriendServiceImpl.buildFriendRequest(it, true) })
        }.build(), events)
    }

    private suspend fun buildFriendRequest(request: FriendRequest, outgoing: Boolean = false): com.lunarclient.websocket.friend.v1.FriendRequest {
        val recipient = userRepository.findById(if (outgoing) request.senderId else request.recipientId).awaitFirst()
        return com.lunarclient.websocket.friend.v1.FriendRequest.newBuilder().apply {
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
        friend: OfflineFriend,
        friendUser: User? = null,
        bot: Boolean = false
    ): FriendStatusPush =
        FriendStatusPush.newBuilder().apply {
            this.onlineFriend = this@FriendServiceImpl.buildOnlineFriend(friend, friendUser, bot)
        }.build()

    private suspend fun buildOnlineFriend(
        friend: OfflineFriend,
        friendUser: User? = null,
        bot: Boolean = false
    ): OnlineFriend {
        val friendUuid = friend.player.uuid.toUUIDString()
        return OnlineFriend.newBuilder().apply {
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
                this.minecraftVersion = MinecraftVersion.newBuilder().setEnum(it).build()
            }
            sessionService.getLocation(friendUuid)?.let {
                this.location = it
            }
            if (bot) {
                this.location = this@FriendServiceImpl.buildBotLocation()
            }
        }.build()
    }

    private fun buildBotLocation() = Location.newBuilder().apply {
        this.publicServer = PublicServer.newBuilder().apply {
            this.serverMappingsId = "localhost"
            this.name = "type .help for help"
        }.build()
    }.build()

    /**
     * Process friend add request
     * */
    override suspend fun processAddFriendRequest(
        message: SendFriendRequestRequest,
        user: User
    ): GeneratedMessage {
        val targetUsername = message.targetUsername

        logger.info { "User ${user.username} send friend request to $targetUsername" }

        if (user.username.equals(targetUsername, ignoreCase = true)) {
            return buildResponse(
                user,
                SendFriendRequestResponse.Status.STATUS_TARGET_IS_SENDER
            )
        }

        val targetUser = userRepository.findByUsernameIgnoreCase(targetUsername)
            .awaitFirstOrNull()



        when {
            targetUser == null -> return buildResponse(
                targetUsername,
                SendFriendRequestResponse.Status.STATUS_TARGET_INVALID_USERNAME
            )

            !targetUser.allowFriendRequests -> return buildResponse(
                targetUser,
                SendFriendRequestResponse.Status.STATUS_TARGET_FRIEND_REQUESTS_DISABLED
            )

            hasFriend(user, targetUser) -> return buildResponse(
                targetUser,
                SendFriendRequestResponse.Status.STATUS_ALREADY_FRIENDS
            )

            hasInboundFriendRequests(user, targetUser) -> return buildResponse(
                targetUser,
                SendFriendRequestResponse.Status.STATUS_ALREADY_HAVE_INBOUND_REQUEST
            )

            hasOutboundFriendRequests(user, targetUser) -> return buildResponse(
                targetUser,
                SendFriendRequestResponse.Status.STATUS_ALREADY_HAVE_OUTBOUND_REQUEST
            )

            else -> {
                sendFriendRequest(user, targetUser)
                return buildResponse(
                    targetUser,
                    SendFriendRequestResponse.Status.STATUS_OK
                )
            }
        }
    }

    override suspend fun processToggleFriendRequests(
        message: ToggleFriendRequestsRequest,
        user: User
    ): GeneratedMessage {
        logger.info { "User ${if (message.allowFriendRequests) "enabled" else "disabled"} incoming friend requests" }
        user.allowFriendRequests = message.allowFriendRequests
        // save user
        userRepository.save(user).awaitFirst()
        return ToggleFriendRequestsResponse.getDefaultInstance()
    }

    override suspend fun processBroadcastStatusChange(
        message: BroadcastStatusChangeRequest,
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
                ?.pushEvent(this.buildOnlineFriendStatusPush(target.lunarType, target.friendUser))
        }
        return BroadcastStatusChangeResponse.getDefaultInstance()
    }

    private fun buildBotFriend(user: User): OfflineFriend {
        return OfflineFriend.newBuilder().apply {
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
    ): OfflineFriend {
        return OfflineFriend.newBuilder().apply {
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
            session.pushEvent(FriendRequestReceivedPush.newBuilder().apply {
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
        status: SendFriendRequestResponse.Status
    ): SendFriendRequestResponse {
        return SendFriendRequestResponse.newBuilder()
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
        status: SendFriendRequestResponse.Status
    ): SendFriendRequestResponse {
        return SendFriendRequestResponse.newBuilder()
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
                                session.pushEvent(FriendStatusPush.newBuilder().apply {
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
    val lunarType: OfflineFriend,
    val friendUser: User
)