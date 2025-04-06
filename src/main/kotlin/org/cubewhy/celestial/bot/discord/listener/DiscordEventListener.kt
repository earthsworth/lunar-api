package org.cubewhy.celestial.bot.discord.listener

import discord4j.core.event.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging

interface DiscordEventListener<T : Event> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getEventType(): Class<T>
    suspend fun execute(event: T)

    suspend fun handleError(error: Throwable?) {
        logger.error(error) { "Unable to process ${getEventType().simpleName}" }
    }
}