package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.service.ConversationService
import org.cubewhy.celestial.service.SessionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class IrcCommand(
    private val sessionService: SessionService,
    private val messageRepository: MessageRepository,
    private val conversationService: ConversationService
) : Command {
    @Value("\${lunar.friend.bot.username}")
    var botUsername = "lunar_cn"

    override fun trigger() = "i"
    override fun usage() = "<content>"

    override fun description() = "Send messages to all players that connected to this network"

    override suspend fun execute(user: User, args: List<String>): String? {
        if (args.isEmpty()) {
            return help()
        }
        val content = args.joinToString(" ")
        // push to irc
        conversationService.pushIrc(user.username, content, user.id!!, fromDiscord = false)
        return null
    }
}