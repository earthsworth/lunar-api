package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Song(
    @Id val id: Long,
    val fakeStyngrId: String, // the uuid in the fake styngr api
    val name: String, // in game name
    val image: String, // thumbnail url
    val song: String, // song name
    val artist: String,
    val album: String, // description
    val durationMillis: Int,
)
