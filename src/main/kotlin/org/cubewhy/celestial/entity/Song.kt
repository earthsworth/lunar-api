package org.cubewhy.celestial.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document
data class Song(
    @Id val id: String? = null,
    val uuid: String = UUID.randomUUID().toString(),
    val owner: String,

    var name: String, // in game name
    var thumbnail: String, // image upload id
    var songName: String, // song name
    var artist: String,
    var album: String, // description
    var durationMillis: Int,

    var uploadId: String, // file id
) : TrackingEntity() {
    val numberId: Int
        get() {
            val objectId = ObjectId(this.id!!)
            val timestamp = objectId.timestamp
            return timestamp
        }
}
