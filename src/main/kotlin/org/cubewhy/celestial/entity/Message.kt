package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

@Document
data class Message(
    @Id
    val id: String? = null,

    val lunarclientId: String = UUID.randomUUID().toString(),

    val senderId: String?, // null to bots
    val targetId: String,

    val content: String,

    val timestamp: Instant = Instant.now(),
) {
    companion object {
        fun createBotResponse(content: String, recipient: User): Message {
            return Message(content = content, targetId = recipient.id!!, senderId = null)
        }
    }
}
