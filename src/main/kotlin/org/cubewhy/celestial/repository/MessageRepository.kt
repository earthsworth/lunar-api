package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Message
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface MessageRepository : ReactiveMongoRepository<Message, String> {
    fun findAllBySenderId(senderId: String): Flux<Message>

    fun findAllByTargetId(targetId: String): Flux<Message>
}