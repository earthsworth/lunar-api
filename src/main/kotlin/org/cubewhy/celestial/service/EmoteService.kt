package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.emote.v1.*
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface EmoteService : PacketProcessor {
    suspend fun processLogin(user: User): GeneratedMessage
    suspend fun processUseEmote(
        request: UseEmoteRequest,
        connection: ClientConnection<*>,
        user: User
    ): UseEmoteResponse

    suspend fun processStopEmote(
        connection: ClientConnection<*>,
        user: User
    ): StopEmoteResponse

    suspend fun processUpdateEquippedEmotes(
        request: UpdateEquippedEmotesRequest,
        connection: ClientConnection<*>,
        user: User
    ): UpdateEquippedEmotesResponse

    fun refreshEmote(
        user: User
    )
}