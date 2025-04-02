package org.cubewhy.celestial.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class CoroutineConfig {
    @Bean
    open fun coroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO)
    }
}