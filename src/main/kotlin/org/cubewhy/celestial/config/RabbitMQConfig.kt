package org.cubewhy.celestial.config

import com.rabbitmq.stream.Environment
import com.rabbitmq.stream.ProducerBuilder
import com.rabbitmq.stream.StreamCreator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate
import org.springframework.rabbit.stream.support.StreamAdmin
import java.time.Duration


@Configuration
class RabbitMQConfig {
    @Bean
    fun streamAdmin(env: Environment): StreamAdmin {
        return StreamAdmin(env) { sc: StreamCreator ->
            sc.stream("stream.lunar.queue1").maxAge(Duration.ofMinutes(5)).create()
        }
    }

    @Bean
    fun streamTemplate(env: Environment): RabbitStreamTemplate {
        val template = RabbitStreamTemplate(env, "stream.lunar.queue1")
        template.setProducerCustomizer { _: String?, builder: ProducerBuilder -> builder.name("data") }
        return template
    }
}