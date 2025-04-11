package org.cubewhy.celestial.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Song(
    @Id val id: String? = null,
    val name: String, // in game name
    val image: String, // image upload id
    val song: String, // song name
    val artist: String,
    val album: String, // description
    val durationMillis: Int,

    val uploadId: String, // file id
) : TrackingEntity() {
    val numberId: Int
        get() {
            val objectId = ObjectId(this.id!!)
            val timestamp = objectId.timestamp
            return timestamp
        }
}
