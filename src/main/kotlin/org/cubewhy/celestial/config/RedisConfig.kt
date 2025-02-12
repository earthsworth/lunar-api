package org.cubewhy.celestial.config

import org.cubewhy.celestial.entity.OnlineUser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializationContext.RedisSerializationContextBuilder
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(
        factory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, OnlineUser> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer: Jackson2JsonRedisSerializer<OnlineUser> =
            Jackson2JsonRedisSerializer(OnlineUser::class.java)
        val builder: RedisSerializationContextBuilder<String, OnlineUser> =
            RedisSerializationContext.newSerializationContext(keySerializer)
        val context: RedisSerializationContext<String, OnlineUser> =
            builder.value(valueSerializer).build()
        return ReactiveRedisTemplate(factory, context)
    }
}