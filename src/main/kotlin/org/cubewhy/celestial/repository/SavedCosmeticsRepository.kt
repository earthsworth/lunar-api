package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.SavedCosmetics
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SavedCosmeticsRepository : ReactiveMongoRepository<SavedCosmetics, String>