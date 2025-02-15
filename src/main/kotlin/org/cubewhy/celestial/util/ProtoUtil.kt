package org.cubewhy.celestial.util

import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1.AuthSuccessMessage
import com.lunarclient.common.v1.LunarclientCommonV1
import com.lunarclient.common.v1.LunarclientCommonV1.UuidAndUsername
import com.lunarclient.websocket.conversation.v1.WebsocketConversationV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1.WebSocketRpcResponse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.Message
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.util.*


/**
 * Wrap an assets server packet with LunarClient wrapper
 *
 * @param requestId clientside request id
 * */
fun GeneratedMessage.wrapCommon(requestId: ByteString): WebsocketProtocolV1.ClientboundWebSocketMessage {
    return WebsocketProtocolV1.ClientboundWebSocketMessage.newBuilder()
        .setRpcResponse(
            WebSocketRpcResponse.newBuilder()
                .setRequestId(requestId)
                .setOutput(this.toByteString())
                .build()
        )
        .build()
}


fun GeneratedMessage.wrapPush(): WebsocketProtocolV1.ClientboundWebSocketMessage {
    return WebsocketProtocolV1.ClientboundWebSocketMessage.newBuilder().apply {
        pushNotification = Any.pack(this@wrapPush)
    }.build()
}

fun AuthSuccessMessage.wrapAuthenticator(): LunarclientAuthenticatorV1.ClientboundWebSocketMessage {
    return LunarclientAuthenticatorV1.ClientboundWebSocketMessage.newBuilder()
        .setAuthSuccess(this)
        .build()
}

fun LunarclientCommonV1.Uuid.toUUIDString(): String {
    return UUID(this.high64, this.low64).toString()
}

fun Instant.toProtobufType(): Timestamp = Timestamp.newBuilder()
    .setSeconds(epochSecond)
    .setNanos(nano)
    .build()

fun Int.toLunarClientColor(): LunarclientCommonV1.Color =
    LunarclientCommonV1.Color.newBuilder().apply {
        color = this@toLunarClientColor
    }.build()

fun UUID.toLunarClientUUID(): LunarclientCommonV1.Uuid = LunarclientCommonV1.Uuid.newBuilder().apply {
    this.high64 = this@toLunarClientUUID.mostSignificantBits
    this.low64 = this@toLunarClientUUID.leastSignificantBits
}.build()

fun String.toLunarClientUUID() = UUID.fromString(this).toLunarClientUUID()

suspend fun WebSocketSession.pushEvent(event: GeneratedMessage) {
    val payload = event
        .wrapPush()
        .toByteArray()
    this.send(this.binaryMessage { it.wrap(payload) }.toMono()).awaitFirstOrNull()
}

val botUuid: UUID = UUID.fromString("1f133c76-fc28-463c-9611-f0013e68e529")

fun String.toLunarClientPlayer(bot: Boolean = false): UuidAndUsername {
    return UuidAndUsername.newBuilder().apply {
        username = this@toLunarClientPlayer
        if (bot) {
            uuid = botUuid.toLunarClientUUID()
        }
    }.build()
}

fun GeneratedMessage.toJson(): String = JsonFormat.printer().print(this)

/**
 * Convert a JSON string to the original protobuf message.
 *
 * @param builder The protobuf message builder instance.
 * @return The deserialized protobuf message.
 */
fun <T : GeneratedMessage, B : GeneratedMessage.Builder<B>> String.toProtobufMessage(builder: B): T {
    JsonFormat.parser().merge(this, builder)
    @Suppress("UNCHECKED_CAST")
    return builder.build() as T
}

fun Message.buildBotResponsePush(botUsername: String): List<WebsocketConversationV1.ConversationMessagePush> {
    return this.content.split("\n").map { line ->
        WebsocketConversationV1.ConversationMessagePush.newBuilder().apply {
            this.message = WebsocketConversationV1.ConversationMessage.newBuilder().apply {
                this.id = this@buildBotResponsePush.lunarclientId.toLunarClientUUID()
                this.contents =
                    WebsocketConversationV1.ConversationMessageContents.newBuilder().setPlainText(line)
                        .build()
                this.sender = WebsocketConversationV1.ConversationSender.newBuilder().apply {
                    this.player = botUsername.toLunarClientPlayer(bot = true)
                }.build()
                this.sentAt = this@buildBotResponsePush.timestamp.toProtobufType()
            }.build()
            this.conversationReference = WebsocketConversationV1.ConversationReference.newBuilder().apply {
                this.friendUuid = botUuid.toLunarClientUUID()
            }.build()
        }.build()
    }
}