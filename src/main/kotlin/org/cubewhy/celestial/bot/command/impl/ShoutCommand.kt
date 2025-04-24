package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.ConversationService
import org.springframework.stereotype.Component

@Component
class ShoutCommand(private val conversationService: ConversationService) : Command {
    override fun trigger() = "s"

    override fun usage() = "<message>"

    override fun description() = "Send messages to IRC, but ignores DND"

    override fun roles() = listOf(Role.ADMIN, Role.STAFF)

    override suspend fun execute(
        user: User,
        args: List<String>
    ): String? {
        // push to irc
        conversationService.pushIrc(user.username, "[SHOUT]", user, force = true)
        return null
    }
}