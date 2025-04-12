package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.BlogPost
import org.cubewhy.celestial.entity.vo.BlogPostVO

interface BlogPostMapper {
    suspend fun mapToBlogPostVO(blogPost: BlogPost, baseUrl: String): BlogPostVO
}