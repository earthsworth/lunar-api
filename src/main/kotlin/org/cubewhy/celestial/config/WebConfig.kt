package org.cubewhy.celestial.config

import org.cubewhy.celestial.handler.websocket.AssetsHandler
import org.cubewhy.celestial.handler.websocket.AuthorizeHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WebConfig(
    private val authorizeHandler: AuthorizeHandler,
    private val assetsHandler: AssetsHandler
) {
    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = mapOf(
            "/ws" to authorizeHandler,
            "/ws/game" to assetsHandler
        )
        val order = -1

        return SimpleUrlHandlerMapping(map, order)
    }
}