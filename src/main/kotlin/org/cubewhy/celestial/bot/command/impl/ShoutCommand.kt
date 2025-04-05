package org.cubewhy.celestial.bot.command.impl

import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.MessageRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.buildBotResponsePush
import org.cubewhy.celestial.util.pushEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ShoutCommand(
    private val sessionService: SessionService,
    private val messageRepository: MessageRepository
) : Command {
    @Value("\${lunar.friend.bot.username}")
    var botUsername = "lunar_cn"

    override fun trigger() = "shout"
    override fun usage() = "<content>"
    override fun roles() = listOf(Role.ADMIN)

    override fun description() = "Send messages to all players that connected to this network"

    override suspend fun execute(user: User, args: List<String>): String {
        if (args.isEmpty()) {
            return help()
        }
        val content = args.joinToString(" ")
        sessionService.pushAll { target, session ->
            // push message
            val message = messageRepository.save(Message.createBotResponse("[shout] $content", target)).awaitFirst()
            message.buildBotResponsePush(botUsername).forEach { push ->
                session.pushEvent(push)
            }
        }
        return "Success"
    }
}