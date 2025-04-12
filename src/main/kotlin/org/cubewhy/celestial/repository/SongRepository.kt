package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Song
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface SongRepository : ReactiveMongoRepository<Song, String> {
    fun findByUuid(uuid: UUID): Mono<Song>
    fun findAllByOwner(owner: String): Flux<Song>
}