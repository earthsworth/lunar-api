package org.cubewhy.celestial.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.cubewhy.celestial.entity.UserSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig {

    @Bean
    fun reactiveRedisTemplate(
        objectMapper: ObjectMapper,
        factory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, UserSession> {
        val serializer = Jackson2JsonRedisSerializer(objectMapper, UserSession::class.java)
        val context = RedisSerializationContext.newSerializationContext<String, UserSession>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(factory, context)
    }
}