package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.chat.v1.WebsocketChatV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.service.MessageService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.pushEvent
import org.cubewhy.celestial.util.toLunarClientUUID
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class MessageServiceImpl(
    private val userRepository: UserRepository,
    private val friendService: FriendService,
    private val messageRepository: MessageRepository,
    private val sessionService: SessionService
) : MessageService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        return when (method) {
            "SendChat" -> this.processSendMessage(
                WebsocketChatV1.SendChatRequest.parseFrom(payload),
                user,
                session
            )

            else -> null
        }
    }

    override suspend fun processSendMessage(
        request: WebsocketChatV1.SendChatRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketChatV1.SendChatResponse {
        // todo: process botMessage
        val target = userRepository.findByUuid(request.targetUuid.toUUIDString()).awaitFirst()
        if (!friendService.hasFriend(user, target)) {
            // no friends
            // I don't know how lunarclient process this
            return WebsocketChatV1.SendChatResponse.getDefaultInstance()
        }
        logger.info { "User ${user.username} send message to ${target.username} (${request.chatMessage})" }
        // save chat message
        messageRepository.save(Message(null, user.id!!, target.id!!, request.chatMessage)).awaitFirst()
        // push chat message
        sessionService.getSession(user)?.pushEvent(this.buildReceiveChatPush(user, request.chatMessage))
        return WebsocketChatV1.SendChatResponse.getDefaultInstance()
    }

    private fun buildReceiveChatPush(
        sender: User,
        message: String
    ): WebsocketChatV1.ReceiveChatPush {
        return WebsocketChatV1.ReceiveChatPush.newBuilder().apply {
            senderUuid = sender.uuid.toLunarClientUUID()
            chatMessage = message
        }.build()
    }
}