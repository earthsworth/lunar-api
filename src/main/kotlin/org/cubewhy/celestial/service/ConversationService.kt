package org.cubewhy.celestial.service

import com.lunarclient.websocket.conversation.v1.SendConversationMessageRequest
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface ConversationService : PacketProcessor {
    suspend fun processSendMessage(
        request: SendConversationMessageRequest,
        user: User,
        connection: ClientConnection<*>
    ): RpcResponse

    suspend fun pushIrc(
        nickname: String,
        content: String,
        self: User? = null,
        fromDiscord: Boolean = false,
        force: Boolean = false
    )

    suspend fun muteUserInIrc(username: String)
    suspend fun unmuteUserInIrc(username: String)
    suspend fun toggleDND(user: User): User
}