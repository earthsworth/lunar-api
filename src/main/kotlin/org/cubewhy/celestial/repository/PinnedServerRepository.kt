package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.PinnedServer
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PinnedServerRepository : ReactiveMongoRepository<PinnedServer, String>