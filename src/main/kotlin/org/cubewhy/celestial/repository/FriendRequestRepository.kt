package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.FriendRequest
import org.springframework.data.mongodb.repository.DeleteQuery
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface FriendRequestRepository : ReactiveCrudRepository<FriendRequest, String> {
    fun existsBySenderIdAndRecipientId(senderId: String, recipientId: String): Mono<Boolean>
    fun findAllByRecipientId(recipientId: String): Flux<FriendRequest>
    fun findAllBySenderId(senderId: String): Flux<FriendRequest>

    @DeleteQuery("{ \$or: [ { 'senderId': ?0, 'recipientId': ?1 }, { 'senderId': ?1, 'recipientId': ?0 } ] }")
    fun deleteBySenderIdAndRecipientId(senderId: String, recipientId: String): Mono<Boolean>
}