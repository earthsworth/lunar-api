package org.cubewhy.celestial.util

import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import com.google.protobuf.util.JsonFormat
import com.lunarclient.authenticator.v1.AuthSuccessMessage
import com.lunarclient.authenticator.v1.EncryptionRequestMessage
import com.lunarclient.common.v1.Color
import com.lunarclient.common.v1.Uuid
import com.lunarclient.common.v1.UuidAndUsername
import com.lunarclient.websocket.conversation.v1.*
import com.lunarclient.websocket.protocol.v1.WebSocketRpcResponse
import org.cubewhy.celestial.entity.Message
import java.time.Instant
import java.util.*
import com.lunarclient.authenticator.v1.ClientboundWebSocketMessage as AuthenticatorClientboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ClientboundWebSocketMessage as AssetsClientboundWebsocketMessage
import com.lunarclient.websocket.protocol.v1.ServerboundWebSocketMessage as RpcServerboundWebSocketMessage


fun GeneratedMessage.wrapCommonServer(
    requestId: ByteString,
    serviceName: String,
    methodName: String
): RpcServerboundWebSocketMessage {
    return RpcServerboundWebSocketMessage.newBuilder()
        .setRequestId(requestId)
        .setService(serviceName)
        .setMethod(methodName)
        .setInput(this.toByteString())
        .build()
}

/**
 * Wrap an assets server packet with LunarClient wrapper
 *
 * @param requestId clientside request id
 * */
fun GeneratedMessage.wrapCommonClient(requestId: ByteString): AssetsClientboundWebsocketMessage {
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

fun GeneratedMessage.wrapAuthenticator(): AuthenticatorClientboundWebsocketMessage {
    return AuthenticatorClientboundWebsocketMessage.newBuilder().apply {
        when (this@wrapAuthenticator) {
            is AuthSuccessMessage -> this.authSuccess = this@wrapAuthenticator
            is EncryptionRequestMessage -> this.encryptionRequest = this@wrapAuthenticator
            else -> throw IllegalArgumentException("Unsupported authenticator type ${this@wrapAuthenticator.descriptorForType}")
        }
    }.build()
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
                this.sentAt = this@buildBotResponsePush.createdAt.toProtobufType()
            }.build()
            this.conversationReference = ConversationReference.newBuilder().apply {
                this.friendUuid = botUuid.toLunarClientUUID()
            }.build()
        }.build()
    }
}