package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.buildBotResponsePush
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class IrcCommand(
    private val sessionService: SessionService,
    private val messageRepository: MessageRepository
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
        val content = "${user.username} > ${args.joinToString(" ")}"
        this.pushIRCMessage(user, content)
        return null
    }

    private suspend fun pushIRCMessage(self: User, content: String) {
        sessionService.pushAll { target ->
            if (self.id!! != target.id) {
                // build message
                // To reduce the database size, no irc messages is stored.
                val message = Message.createBotResponse("[irc] $content", target)
                message.buildBotResponsePush(botUsername).forEach { push ->
                    // push
                    sessionService.push(target, push)
                }
            }
        }
        // TODO push to Discord if available
    }
}