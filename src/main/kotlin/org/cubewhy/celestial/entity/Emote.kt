package org.cubewhy.celestial.entity

import com.lunarclient.websocket.emote.v1.OwnedEmote
import org.cubewhy.celestial.util.toProtobufType
import java.time.Instant

data class Emote(
    val emoteId: Int,
    val name: String,
) {
    fun toOwnedEmote(emote: Int): OwnedEmote {
        return OwnedEmote.newBuilder().apply {
            emoteId = emote
            grantedAt = Instant.now().toProtobufType()
        }.build()
    }
}
