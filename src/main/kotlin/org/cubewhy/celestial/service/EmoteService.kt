package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.emote.v1.StopEmoteResponse
import com.lunarclient.websocket.emote.v1.UpdateEquippedEmotesRequest
import com.lunarclient.websocket.emote.v1.UpdateEquippedEmotesResponse
import com.lunarclient.websocket.emote.v1.UseEmoteRequest
import com.lunarclient.websocket.emote.v1.UseEmoteResponse
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface EmoteService : PacketProcessor {
    suspend fun processLogin(user: User): GeneratedMessage
    suspend fun processUseEmote(
        request: UseEmoteRequest,
        session: WebSocketSession,
        user: User
    ): UseEmoteResponse

    suspend fun processStopEmote(
        session: WebSocketSession,
        user: User
    ): StopEmoteResponse

    suspend fun processUpdateEquippedEmotes(
        request: UpdateEquippedEmotesRequest,
        session: WebSocketSession,
        user: User
    ): UpdateEquippedEmotesResponse
}