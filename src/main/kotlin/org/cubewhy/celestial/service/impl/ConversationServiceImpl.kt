package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.conversation.v1.*
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.core.spec.EmbedCreateSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CommandService
import org.cubewhy.celestial.service.ConversationService
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Instant


@Service
class ConversationServiceImpl(
    private val userRepository: UserRepository,
    private val friendService: FriendService,
    private val messageRepository: MessageRepository,
    private val sessionService: SessionService,
    @Lazy
    private val commandService: CommandService,
    @Lazy
    @Autowired(required = false)
    private val gatewayDiscordClient: GatewayDiscordClient?,

    private val lunarProperties: LunarProperties
) : ConversationService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Value("\${lunar.friend.bot.username}")
    var botUsername = "lunar_cn"

    override val serviceName: String = "lunarclient.websocket.conversation.v1.ConversationService"

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "SendConversationMessage" -> this.processSendMessage(
                SendConversationMessageRequest.parseFrom(payload),
                user,
                session
            )

            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processSendMessage(
        request: SendConversationMessageRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketResponse {
        val recipientUuid = request.conversationReference.friendUuid.toUUIDString()
        val chatMessage = request.messageContents.plainText
        // is bot
        if (recipientUuid == botUuid.toString()) {
            // process bot commands
            logger.info { "User ${user.username} send message to bot: $chatMessage" }
            val msg = Message(
                senderId = user.id!!,
                targetId = user.id,
                content = chatMessage,
            )
            val events =
                mutableListOf(this.buildConversationMessagePush(msg, user, request.conversationReference))
            // process command
            commandService.process(chatMessage, user)?.let { responseMsg ->
                // build pushes
                events.addAll(responseMsg.buildBotResponsePush(botUsername))
            }
            return SendConversationMessageResponse.newBuilder().apply {
                status =
                    SendConversationMessageResponse.Status.STATUS_OK
            }.build().toWebsocketResponse().addPush(events.map { pushOf(it) })
        }
        val target = userRepository.findByUuid(recipientUuid).awaitFirst()
        if (!friendService.hasFriend(user, target)) {
            // not friends
            return SendConversationMessageResponse.newBuilder().apply {
                status = SendConversationMessageResponse.Status.STATUS_UNKNOWN_CONVERSATION
            }.build().toWebsocketResponse()
        }
        logger.info { "User ${user.username} send message to ${target.username} (${chatMessage})" }
        // save chat message
        val savedMessage = messageRepository.save(
            Message(
                senderId = user.id!!,
                targetId = target.id!!,
                content = chatMessage,
            )
        ).awaitFirst()
        // push chat message
        sessionService.push(
            target,
            this.buildConversationMessagePush(savedMessage, user, ConversationReference.newBuilder().apply {
                this.friendUuid = user.uuid.toLunarClientUUID()
            }.build())
        ) // push to recipient
        session.pushEvent(
            this.buildConversationMessagePush(
                savedMessage,
                user,
                request.conversationReference
            )
        ) // push to self
        return SendConversationMessageResponse.newBuilder().apply {
            status =
                SendConversationMessageResponse.Status.STATUS_OK
        }.build().toWebsocketResponse()
    }

    override suspend fun pushIrc(nickname: String, content: String, self: User?, fromDiscord: Boolean) {
        sessionService.pushAll { target ->
            if (self?.id != target.id) {
                // build message
                // To reduce the database size, no irc messages is stored.
                val message =
                    Message.createBotResponse("[irc] ${if (fromDiscord) "[DC] " else ""}$nickname > $content", target)
                message.buildBotResponsePush(botUsername).forEach { push ->
                    // push
                    sessionService.push(target, push)
                }
            }
        }
        if (!fromDiscord && gatewayDiscordClient != null) {
            // push to discord
            val channelId = Snowflake.of(lunarProperties.discord.irc.channel)
            val embed = EmbedCreateSpec.builder()
                .color(discord4j.rest.util.Color.of(self!!.logoColor.color))
                .author(nickname, null, "https://skins.mcstats.com/skull/${self.uuid}")
                .description(content)
                .timestamp(Instant.now())
                .footer("Powered by Celestial", "https://lunarclient.top/favicon.webp")
                .build()

            gatewayDiscordClient.getChannelById(channelId)
                .ofType(GuildMessageChannel::class.java)
                .flatMap { channel ->
                    logger.info { "IRC -> Discord: $nickname > $content" }
                    channel.createMessage(embed)
                }
                .subscribe()
        }
    }

    override suspend fun muteUserInIrc(username: String) {
        // find user
        val user = userRepository.findByUsernameIgnoreCase(username).awaitFirstOrNull()
            ?: throw IllegalArgumentException("Unknown user $username")
        if (user.irc.muted) throw IllegalArgumentException("This user is already muted")
        // check permission
        if (user.roles.contains(Role.ADMIN) || user.roles.contains(Role.STAFF)) throw IllegalArgumentException("You cannot mute this user")
        logger.info { "Mute user ${user.username} in irc" }
        user.irc.muted = true
        // save user
        userRepository.save(user).awaitFirst()
    }

    override suspend fun unmuteUserInIrc(username: String) {
        // find user
        val user = userRepository.findByUsernameIgnoreCase(username).awaitFirstOrNull()
            ?: throw IllegalArgumentException("Unknown user $username")
        if (!user.irc.muted) throw IllegalArgumentException("This user is not muted")
        logger.info { "Unmute user ${user.username} in irc" }
        user.irc.muted = false
        // save user
        userRepository.save(user).awaitFirst()
    }

    private fun buildConversationMessagePush(
        message: Message,
        sender: User,
        conversationReference: ConversationReference,
    ): ConversationMessagePush {
        return ConversationMessagePush.newBuilder().apply {
            this.message =
                this@ConversationServiceImpl.buildConversationMessage(message, sender)
            this.conversationReference = conversationReference
        }.build()
    }

    private fun buildConversationMessage(
        message: Message,
        sender: User
    ) = ConversationMessage.newBuilder().apply {
        this.id = message.lunarclientId.toLunarClientUUID()
        this.contents =
            ConversationMessageContents.newBuilder().setPlainText(message.content).build()
        this.sender = ConversationSender.newBuilder().apply {
            this.player = sender.toLunarClientPlayer()
        }.build()
        this.sentAt = message.createdAt.toProtobufType()
    }.build()
}