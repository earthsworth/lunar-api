package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.BlogPost
import org.cubewhy.celestial.entity.vo.BlogPostVO
import org.cubewhy.celestial.service.BlogPostMapper
import org.springframework.stereotype.Service

@Service
class BlogPostMapperImpl : BlogPostMapper {
    override suspend fun mapToBlogPostVO(blogPost: BlogPost, baseUrl: String): BlogPostVO {
        return BlogPostVO(
            title = blogPost.title,
            image = "${baseUrl}api/upload?id=${blogPost.image}",
            link = blogPost.link,
            createdAt = blogPost.createdAt.epochSecond
        )
    }
}