package org.cubewhy.celestial.entity

import com.lunarclient.websocket.emote.v1.EquippedEmote
import com.lunarclient.websocket.emote.v1.OwnedEmote
import org.cubewhy.celestial.util.toProtobufType
import java.time.Instant

data class Emote(
    val emoteId: Int,
    val slotNumber: Int,
    val attachedJamId: Int
) {
    fun toOwnedEmote(): OwnedEmote {
        return OwnedEmote.newBuilder().apply {
            this.emoteId = this@Emote.emoteId
            this.grantedAt = Instant.now().toProtobufType()
        }.build()
    }

    fun toEquippedEmote(): EquippedEmote {
        return EquippedEmote.newBuilder().apply {
            this.emoteId = this@Emote.emoteId
            this.slotNumber = this@Emote.slotNumber
            this.attachedJamId = this@Emote.attachedJamId
        }.build()
    }
}
