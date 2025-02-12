package org.cubewhy.celestial.entity

import com.lunarclient.websocket.emote.v1.WebsocketEmoteV1
import org.cubewhy.celestial.util.calcTimestamp
import java.time.Instant

data class Emote(
    val emoteId: Int,
    val name: String,
){
    fun toOwnedEmote(emote: Int) : WebsocketEmoteV1.OwnedEmote {
        return WebsocketEmoteV1.OwnedEmote.newBuilder().apply {
            emoteId = emote
            expiresAt = calcTimestamp(Instant.MAX)
            grantedAt = calcTimestamp(Instant.MIN)
        }.build()
    }
}
