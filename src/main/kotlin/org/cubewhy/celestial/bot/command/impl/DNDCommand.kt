package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.ConversationService
import org.springframework.stereotype.Component

@Component
class DNDCommand(private val conversationService: ConversationService) : Command {
    override fun trigger() = "dnd"

    override fun usage() = ""

    override fun description() = "Toggle DO NOT DISTURB for the global irc"

    override suspend fun execute(
        user: User,
        args: List<String>
    ): String {
        val newState = conversationService.toggleDND(user).irc.dnd
        return "Success ${if (newState) "enabled" else "disabled"} DND"
    }

}