package org.cubewhy.celestial.config

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.bot.discord.listener.DiscordEventListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnProperty(name = ["lunar.discord.token"])
class DiscordConfig {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Value("\${lunar.discord.token}")
    var discordToken: String = ""

    @Bean
    fun <T : Event> gatewayDiscordClient(eventListeners: List<DiscordEventListener<T>>): GatewayDiscordClient? {
        logger.info { "Logging in to Discord" }
        val client = DiscordClientBuilder.create(discordToken)
            .build()
            .login()
            .block()
        for (listener in eventListeners) {
            // register event listeners
            client!!.on(listener.getEventType())
                .flatMap {
                    mono { listener.execute(it) }
                }
                .onErrorResume {
                    mono { listener.handleError(it) }
                }
                .subscribe()
        }
        return client
    }
}