package org.cubewhy.celestial

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.cubewhy.celestial.entity.config.InstanceProperties
import org.cubewhy.celestial.entity.config.LunarProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(LunarProperties::class, InstanceProperties::class)
class LunarApiApplication {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun init() {
        logger.info { "Powered by LunarCN/Celestial. https://lunarclient.top" }
        logger.info { "Not affiliated with Moonsworth." }
    }
}

fun main(args: Array<String>) {
    runApplication<LunarApiApplication>(*args)
}
