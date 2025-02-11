package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.FriendRequest
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface FriendRequestRepository: ReactiveCrudRepository<FriendRequest, String> {
    fun existsBySenderIdAndRecipientId(senderId: String, recipientId: String): Mono<Boolean>
}