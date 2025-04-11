package org.cubewhy.celestial.config

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.bot.discord.listener.DiscordEventListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnProperty(name = ["lunar.discord.enabled"], havingValue = "true")
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
            .gateway()
            .setEnabledIntents(IntentSet.of(Intent.GUILDS, Intent.GUILD_MEMBERS, Intent.GUILD_MESSAGES))
            .login()
            .block()

        client?.updatePresence(ClientPresence.online(
            ClientActivity.playing("LunarClient")
        ))?.block()
        for (listener in eventListeners) {
            // register event listeners
            logger.debug { "Register Discord event listener ${listener::class.java.name}" }
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