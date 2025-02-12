package org.cubewhy.celestial.event

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
data class EventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun publish(event: ApplicationEvent) {
        logger.debug { "Publishing event ${event.javaClass.name}" }
        applicationEventPublisher.publishEvent(event)
    }
}