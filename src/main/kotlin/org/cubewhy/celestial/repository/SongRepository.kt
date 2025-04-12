package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Song
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SongRepository : ReactiveMongoRepository<Song, String> {
    fun findByUuid(uuid: String): Mono<Song>
}