package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.Upload
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UploadRepository : ReactiveMongoRepository<Upload, String> {
    fun findBySha256(hash: String): Mono<Upload>
}