package org.cubewhy.celestial

import org.cubewhy.celestial.entity.config.InstanceProperties
import org.cubewhy.celestial.entity.config.LunarProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableReactiveMongoAuditing
@EnableConfigurationProperties(LunarProperties::class, InstanceProperties::class)
class LunarApiApplication

fun main(args: Array<String>) {
    runApplication<LunarApiApplication>(*args)
}
