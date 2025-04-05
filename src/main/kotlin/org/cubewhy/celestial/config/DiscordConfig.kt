package org.cubewhy.celestial.config

import dev.kord.core.Kord
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lunar.discord.token"])
class DiscordConfig {
    @Value("\${lunar.discord.token}")
    var discordToken: String = ""

    @Bean
    suspend fun kord(): Kord = Kord(discordToken)
}