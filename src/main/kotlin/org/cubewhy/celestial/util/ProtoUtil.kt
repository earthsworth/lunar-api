package org.cubewhy.celestial.util

import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1.AuthSuccessMessage
import com.lunarclient.common.v1.LunarclientCommonV1
import com.lunarclient.common.v1.LunarclientCommonV1.UuidAndUsername
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1.WebSocketRpcResponse
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono
import org.cubewhy.celestial.entity.User
import org.springframework.lang.Contract
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


/**
 * Wrap an assets server packet with LunarClient wrapper
 *
 * @param requestId clientside request id
 * */
fun GeneratedMessage.wrapCommon(requestId: ByteString): WebsocketProtocolV1.ClientboundWebSocketMessage {
    return WebsocketProtocolV1.ClientboundWebSocketMessage.newBuilder()
        .setRpcResponse(WebSocketRpcResponse.newBuilder()
            .setRequestId(requestId)
            .setOutput(this.toByteString())
            .build())
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

fun toUuidAndUsername(username: String?): UuidAndUsername {
    return UuidAndUsername.newBuilder()
        .setUsername(username)
        .build()
}

fun toUuidAndUsername(username: String?, uuid: String): UuidAndUsername {
    val parsedUUID = UUID.fromString(uuid)
    return UuidAndUsername.newBuilder()
        .setUsername(username)
        .setUuid(
            LunarclientCommonV1.Uuid.newBuilder()
                .setHigh64(parsedUUID.mostSignificantBits)
                .setLow64(parsedUUID.leastSignificantBits)
        )
        .build()
}

fun toUuidAndUsername(user: User): UuidAndUsername {
    return toUuidAndUsername(user.username, user.uuid)
}

/**
 * Convent a Instant to LunarClient timestamp
 *
 * @param instant Instant object
 * @return timestamp message
 */
@Contract("_ -> new")
fun calcTimestamp(instant: Instant): Timestamp {
    return Timestamp.newBuilder()
        .setNanos(instant.nano)
        .setSeconds(instant.epochSecond)
        .build()
}