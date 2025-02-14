package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.WebUser
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface WebUserRepository : ReactiveMongoRepository<WebUser, String> {
    fun findByUsername(username: String): Mono<WebUser>
    fun countByRole(role: Role): Mono<Long>
}