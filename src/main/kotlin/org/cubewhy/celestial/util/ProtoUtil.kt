package org.cubewhy.celestial.util

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.Timestamp
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1
import com.lunarclient.authenticator.v1.LunarclientAuthenticatorV1.AuthSuccessMessage
import com.lunarclient.common.v1.LunarclientCommonV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1
import com.lunarclient.websocket.protocol.v1.WebsocketProtocolV1.WebSocketRpcResponse
import java.time.Instant
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