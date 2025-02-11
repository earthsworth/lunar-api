package org.cubewhy.celestial

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LunarApiApplication {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun init() {
        logger.info { "Initializing Lunar API servlet" }
        logger.info { "Powered by Celestial. https://lunarclient.top" }
    }
}

fun main(args: Array<String>) {
    runApplication<LunarApiApplication>(*args)
}
