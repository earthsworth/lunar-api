package org.cubewhy.celestial.bot.discord.listener.impl

import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.discord.listener.DiscordEventListener
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.service.ConversationService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class MessageCreateEventListener(
    private val lunarProperties: LunarProperties,
    @Lazy
    private val conversationService: ConversationService
) : DiscordEventListener<MessageCreateEvent> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getEventType() = MessageCreateEvent::class.java

    override suspend fun execute(event: MessageCreateEvent) {
        val message = event.message
        val channelId = message.channel.awaitFirst().id
        if (channelId.asLong() == lunarProperties.discord.irc.channel && event.member.isPresent && message.content.isNotBlank()) {
            val member = event.member.get()
            val nickname = member.nickname.orElseGet { member.username }
            logger.info { "Discord -> IRC: $nickname -> ${message.content}" }
            // push to irc
            conversationService.pushIrc(nickname, message.content, fromDiscord = true)
        }
    }
}