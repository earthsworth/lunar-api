package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.WebUser
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WebUserRepository : ReactiveMongoRepository<WebUser, String> {
}