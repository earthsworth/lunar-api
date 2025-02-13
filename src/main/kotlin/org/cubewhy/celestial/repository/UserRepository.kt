package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveMongoRepository<User, String> {
    fun findByUuid(uuid: String): Mono<User>
    fun findByUsernameIgnoreCase(username: String): Mono<User>
    fun findAllByUuidIn(uuid: Flux<String>): Flux<User>
}