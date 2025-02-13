package org.cubewhy.celestial.service

import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.emote.v1.WebsocketEmoteV1
import org.cubewhy.celestial.entity.User
import org.springframework.web.reactive.socket.WebSocketSession

interface EmoteService : PacketProcessor {
    suspend fun processLogin(user: User): GeneratedMessage
    suspend fun processUseEmote(
        request: WebsocketEmoteV1.UseEmoteRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketEmoteV1.UseEmoteResponse

    suspend fun processStopEmote(
        session: WebSocketSession,
        user: User
    ): WebsocketEmoteV1.StopEmoteResponse

    suspend fun processUpdateEquippedEmotes(
        request: WebsocketEmoteV1.UpdateEquippedEmotesRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketEmoteV1.UpdateEquippedEmotesResponse
}