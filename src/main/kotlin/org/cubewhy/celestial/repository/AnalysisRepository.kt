package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Analysis
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant

@Repository
interface AnalysisRepository : ReactiveMongoRepository<Analysis, String> {
    fun getAnalysisByTimestampAfter(timestamp: Instant): Flux<Analysis>
}