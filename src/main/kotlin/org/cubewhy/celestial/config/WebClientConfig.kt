package org.cubewhy.celestial.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        return WebClient.create()
    }

    @Bean
    fun reactorNettyWebSocketClient(): ReactorNettyWebSocketClient {
        return ReactorNettyWebSocketClient(
            HttpClient.create()
                .headers { builder ->
                    builder.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:141.0) Gecko/20100101 Firefox/141.0")
                })
    }
}