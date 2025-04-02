package org.cubewhy.celestial.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.task.SimpleAsyncTaskExecutor


@Configuration
open class EventConfig {
    @Bean(name = ["applicationEventMulticaster"])
    open fun simpleApplicationEventMulticaster(): ApplicationEventMulticaster {
        val eventMulticaster =
            SimpleApplicationEventMulticaster()

        @Suppress("UsePropertyAccessSyntax")
        eventMulticaster.setTaskExecutor(SimpleAsyncTaskExecutor())
        return eventMulticaster
    }
}