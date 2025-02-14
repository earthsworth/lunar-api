package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Analysis
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AnalysisRepository : ReactiveMongoRepository<Analysis, String> {
}