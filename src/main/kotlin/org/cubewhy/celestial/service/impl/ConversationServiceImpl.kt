package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.conversation.v1.WebsocketConversationV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.CommandManager
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.ConversationService
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.pushEvent
import org.cubewhy.celestial.util.toLunarClientUUID
import org.cubewhy.celestial.util.toProtobufType
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

@Service
class ConversationServiceImpl(
    private val userRepository: UserRepository,
    private val friendService: FriendService,
    private val messageRepository: MessageRepository,
    private val sessionService: SessionService,
    private val commandManager: CommandManager
) : ConversationService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "SendConversationMessage" -> this.processSendMessage(
                WebsocketConversationV1.SendConversationMessageRequest.parseFrom(payload),
                user,
                session
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processSendMessage(
        request: WebsocketConversationV1.SendConversationMessageRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketConversationV1.SendConversationMessageResponse {
        // todo: process botMessage
        val recipientUuid = request.conversationReference.friendUuid.toUUIDString()
        val chatMessage = request.messageContents.plainText
        // is bot
        if (recipientUuid == UUID(0, 0).toString()) {
            // this is bot
            logger.info { "User ${user.username} send message to bot (${chatMessage})" }
            val savedMessage = messageRepository.save(
                Message(
                    senderId = user.id!!,
                    targetId = user.id,
                    message = commandManager.process(chatMessage, user),
                )
            ).awaitFirst()
            session.pushEvent(this.buildConversationMessagePush(savedMessage, user, request)) // push to self
            return WebsocketConversationV1.SendConversationMessageResponse.newBuilder().apply {
                status = WebsocketConversationV1.SendConversationMessageResponse_Status.SENDCONVERSATIONMESSAGERESPONSE_STATUS_STATUS_OK
            }.build()
        }
        val target = userRepository.findByUuid(recipientUuid).awaitFirst()
        if (!friendService.hasFriend(user, target)) {
            // not friends
            return WebsocketConversationV1.SendConversationMessageResponse.newBuilder().apply {
                status =
                    WebsocketConversationV1.SendConversationMessageResponse_Status.SENDCONVERSATIONMESSAGERESPONSE_STATUS_STATUS_UNKNOWN_CONVERSATION
            }.build()
        }
        logger.info { "User ${user.username} send message to ${target.username} (${chatMessage})" }
        // save chat message
        val savedMessage = messageRepository.save(
            Message(
                senderId = user.id!!,
                targetId = target.id!!,
                message = chatMessage,
            )
        ).awaitFirst()
        // push chat message
        sessionService.getSession(target)?.pushEvent(this.buildConversationMessagePush(savedMessage, user, request)) // push to recipient
        session.pushEvent(this.buildConversationMessagePush(savedMessage, user, request)) // push to self
        return WebsocketConversationV1.SendConversationMessageResponse.newBuilder().apply {
            status = WebsocketConversationV1.SendConversationMessageResponse_Status.SENDCONVERSATIONMESSAGERESPONSE_STATUS_STATUS_OK
        }.build()
    }

    private fun buildConversationMessagePush(
        message: Message,
        sender: User,
        request: WebsocketConversationV1.SendConversationMessageRequest,
    ): WebsocketConversationV1.ConversationMessagePush {
        return WebsocketConversationV1.ConversationMessagePush.newBuilder().apply {
            this.message =
                this@ConversationServiceImpl.buildConversationMessage(message, sender, request.messageContents)
            this.conversationReference = request.conversationReference
        }.build()
    }

    private fun buildConversationMessage(
        message: Message,
        sender: User,
        contents: WebsocketConversationV1.ConversationMessageContents
    ) = WebsocketConversationV1.ConversationMessage.newBuilder().apply {
        this.id = message.lunarclientId.toLunarClientUUID()
        this.contents = contents
        this.sender = WebsocketConversationV1.ConversationSender.newBuilder().apply {
            this.player = sender.toLunarClientPlayer()
        }.build()
        this.sentAt = message.timestamp.toProtobufType()
    }.build()
}