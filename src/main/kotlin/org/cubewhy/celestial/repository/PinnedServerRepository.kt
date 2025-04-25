package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.PinnedServer
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
interface PinnedServerRepository : ReactiveMongoRepository<PinnedServer, String> {

    @Aggregation(pipeline = [
        "{ \$sample: { size: ?0 } }"
    ])
    fun findRandomItems(size: Long): Flux<PinnedServer>

    fun findAllByOwner(owner: String): Flux<PinnedServer>

    fun existsByAddress(address: String): Mono<Boolean>
}