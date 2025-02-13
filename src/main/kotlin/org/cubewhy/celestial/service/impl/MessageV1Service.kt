import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.pushEvent
import org.cubewhy.celestial.util.toLunarClientUUID

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.chat.v1.WebsocketChatV1
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.FriendService
import org.cubewhy.celestial.service.MessageV1Service
import org.cubewhy.celestial.util.toUUIDString
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class MessageV1Service(
    private val userRepository: UserRepository,
    private val friendService: FriendService,
    private val messageRepository: MessageRepository,
    private val sessionService: SessionService
) : MessageV1Service {

    override suspend fun processSendMessage(
        request: WebsocketChatV1.SendChatRequest,
        user: User,
        session: WebSocketSession
    ): WebsocketChatV1.SendChatResponse {
        // todo: process botMessage
        val target = userRepository.findByUuid(request.targetUuid.toUUIDString()).awaitFirst()
        if(!friendService.hasFriend(user, target)) {
            return WebsocketChatV1.SendChatResponse.getDefaultInstance()
        }
        messageRepository.save(Message(null, user.id!!, target.id!!, request.chatMessage)).awaitFirst()
        sessionService.getSession(user)?.pushEvent(this.buildReceiveChatPush(user, request.chatMessage))
        return WebsocketChatV1.SendChatResponse.getDefaultInstance()
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        return when(method) {
            "SendChat" -> this.processSendMessage(
                WebsocketChatV1.SendChatRequest.parseFrom(payload),
                user,
                session
            )

            else -> null
        }
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