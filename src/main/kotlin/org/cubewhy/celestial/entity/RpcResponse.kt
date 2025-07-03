package org.cubewhy.celestial.entity

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage

data class RpcResponse(
    var response: GeneratedMessage? = null,
    var pushes: MutableList<Push> = mutableListOf(),
) {
    var requestId: ByteString? = null

    companion object {
        fun create(response: GeneratedMessage?): RpcResponse? {
            if (response == null) return null
            return RpcResponse(response)
        }
    }

//    fun addPush(vararg push: Push): RpcResponse {
//        pushes.addAll(push)
//        return this
//    }

    fun addPush(pushList: List<Push>): RpcResponse {
        pushes.addAll(pushList)
        return this
    }
}

data class Push(
    val payload: GeneratedMessage,
    val broadcast: Boolean
)

fun GeneratedMessage.toWebsocketResponse() =
    RpcResponse(this)

fun pushOf(payload: GeneratedMessage, broadcast: Boolean = true) =
    Push(payload, broadcast)

fun emptyWebsocketResponse() = RpcResponse()