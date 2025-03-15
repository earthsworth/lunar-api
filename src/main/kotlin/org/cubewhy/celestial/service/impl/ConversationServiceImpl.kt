package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.conversation.v1.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CommandService
import org.cubewhy.celestial.service.ConversationService
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class ConversationServiceImpl(
    private val userRepository: UserRepository,
    private val friendService: FriendService,
    private val messageRepository: MessageRepository,
    private val sessionService: SessionService,
    private val commandService: CommandService,
) : ConversationService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Value("\${lunar.friend.bot.username}")
    var botUsername = "lunar_cn"

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
            logger.info { "User ${user.username} send message to bot (${chatMessage})" }
            val savedMessage = messageRepository.save(
                Message(
                    senderId = user.id!!,
                    targetId = user.id,
                    content = chatMessage,
                )
            ).awaitFirst()
            val events = mutableListOf(this.buildConversationMessagePush(savedMessage, user, request.conversationReference))
            events.addAll(
                messageRepository.save(
                    commandService.process(
                        chatMessage,
                        user
                    )
                ).awaitFirst().buildBotResponsePush(botUsername)
            )
            return WebsocketResponse.create(
                SendConversationMessageResponse.newBuilder().apply {
                    status =
                        SendConversationMessageResponse.Status.STATUS_OK
                }.build(),
                events
            )
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
        sessionService.getSession(target)
            ?.pushEvent(this.buildConversationMessagePush(savedMessage, user, ConversationReference.newBuilder().apply {
                this.friendUuid = target.uuid.toLunarClientUUID()
            }.build())) // push to recipient
        session.pushEvent(this.buildConversationMessagePush(savedMessage, user, request.conversationReference)) // push to self
        return SendConversationMessageResponse.newBuilder().apply {
            status =
                SendConversationMessageResponse.Status.STATUS_OK
        }.build().toWebsocketResponse()
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
        this.sentAt = message.timestamp.toProtobufType()
    }.build()
}