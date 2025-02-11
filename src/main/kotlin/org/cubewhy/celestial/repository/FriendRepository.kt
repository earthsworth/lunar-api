package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Friend
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
interface FriendRepository : ReactiveMongoRepository<Friend, String> {
    fun findAllByUser1(user1: String): Flux<Friend>

    @Query("{ \$or: [ { 'user1': ?0 }, { 'user2': ?0 } ] }")
    fun findFriendRelations(user: String): Flux<Friend>

    @Query("{ \$or: [ { 'user1': ?0, 'user2': ?1 }, { 'user1': ?1, 'user2': ?0 } ] }")
    fun findFriendRelation(user: String, target: String): Mono<Friend>
}