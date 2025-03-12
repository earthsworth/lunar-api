package org.cubewhy.celestial.util

import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import com.lunarclient.authenticator.v1.AuthSuccessMessage
import com.lunarclient.authenticator.v1.ClientboundWebSocketMessage as AuthenticatorClientboundWebsocketMessage
import com.lunarclient.common.v1.*
import com.lunarclient.common.v1.UuidAndUsername
import com.lunarclient.websocket.conversation.v1.*
import com.lunarclient.websocket.protocol.v1.*
import com.lunarclient.websocket.protocol.v1.ClientboundWebSocketMessage as AssetsClientboundWebsocketMessage
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
fun GeneratedMessage.wrapCommon(requestId: ByteString): AssetsClientboundWebsocketMessage {
    return AssetsClientboundWebsocketMessage.newBuilder()
        .setRpcResponse(
            WebSocketRpcResponse.newBuilder()
                .setRequestId(requestId)
                .setOutput(this.toByteString())
                .build()
        )
        .build()
}


fun GeneratedMessage.wrapPush(): AssetsClientboundWebsocketMessage {
    return AssetsClientboundWebsocketMessage.newBuilder().apply {
        pushNotification = Any.pack(this@wrapPush)
    }.build()
}

fun AuthSuccessMessage.wrapAuthenticator(): AuthenticatorClientboundWebsocketMessage {
    return AuthenticatorClientboundWebsocketMessage.newBuilder()
        .setAuthSuccess(this)
        .build()
}

fun Uuid.toUUIDString(): String {
    return UUID(this.high64, this.low64).toString()
}

fun Instant.toProtobufType(): Timestamp = Timestamp.newBuilder()
    .setSeconds(epochSecond)
    .setNanos(nano)
    .build()

fun Int.toLunarClientColor(): Color =
    Color.newBuilder().apply {
        color = this@toLunarClientColor
    }.build()

fun UUID.toLunarClientUUID(): Uuid = Uuid.newBuilder().apply {
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

fun Message.buildBotResponsePush(botUsername: String): List<ConversationMessagePush> {
    return this.content.split("\n").map { line ->
        ConversationMessagePush.newBuilder().apply {
            this.message = ConversationMessage.newBuilder().apply {
                this.id = this@buildBotResponsePush.lunarclientId.toLunarClientUUID()
                this.contents =
                    ConversationMessageContents.newBuilder().setPlainText(line)
                        .build()
                this.sender = ConversationSender.newBuilder().apply {
                    this.player = botUsername.toLunarClientPlayer(bot = true)
                }.build()
                this.sentAt = this@buildBotResponsePush.timestamp.toProtobufType()
            }.build()
            this.conversationReference = ConversationReference.newBuilder().apply {
                this.friendUuid = botUuid.toLunarClientUUID()
            }.build()
        }.build()
    }
}