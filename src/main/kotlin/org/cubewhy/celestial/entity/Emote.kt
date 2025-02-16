package org.cubewhy.celestial.entity

import com.lunarclient.websocket.emote.v1.WebsocketEmoteV1
import org.cubewhy.celestial.util.toProtobufType
import java.time.Instant

data class Emote(
    val emoteId: Int,
    val name: String,
) {
    fun toOwnedEmote(emote: Int): WebsocketEmoteV1.OwnedEmote {
        return WebsocketEmoteV1.OwnedEmote.newBuilder().apply {
            emoteId = emote
            grantedAt = Instant.now().toProtobufType()
        }.build()
    }
}
