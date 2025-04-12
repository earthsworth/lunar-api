package org.cubewhy.celestial.repository

import org.cubewhy.celestial.entity.BlogPost
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BlogPostRepository : ReactiveMongoRepository<BlogPost, String>