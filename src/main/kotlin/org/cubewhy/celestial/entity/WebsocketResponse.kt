package org.cubewhy.celestial.entity

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage

data class WebsocketResponse(
    var response: GeneratedMessage? = null,
    var pushes: MutableList<Push> = mutableListOf(),
) {
    var requestId: ByteString? = null

    companion object {
        fun create(response: GeneratedMessage?): WebsocketResponse? {
            if (response == null) return null
            return WebsocketResponse(response)
        }
    }

    fun addPush(vararg push: Push): WebsocketResponse {
        pushes.addAll(push)
        return this
    }

    fun addPush(pushList: List<Push>): WebsocketResponse {
        pushes.addAll(pushList)
        return this
    }
}

data class Push(
    val payload: GeneratedMessage,
    val broadcast: Boolean
)

fun GeneratedMessage.toWebsocketResponse() =
    WebsocketResponse(this)

fun pushOf(payload: GeneratedMessage, broadcast: Boolean = true) =
    Push(payload, broadcast)

fun emptyWebsocketResponse() = WebsocketResponse()