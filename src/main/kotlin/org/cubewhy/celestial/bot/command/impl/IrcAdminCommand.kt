package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.ConversationService
import org.springframework.stereotype.Component

@Component
class IrcAdminCommand(private val conversationService: ConversationService) : Command {
    override fun trigger() = "i"

    override fun usage() = "<mute/unmute>"

    override fun description() = "The command to manage the irc"

    override fun roles() = listOf(Role.ADMIN, Role.STAFF)

    override suspend fun execute(user: User, args: List<String>): String? {
        if (args.isEmpty()) {
            return help()
        }
        if (args.size == 1) {
            return "Unknown sub command"
        }
        if (args[0] == "mute") {
            if (args.size != 2) {
                return "Bad usage. mute <username>"
            }
            val username = args[1]
            if (username.equals(user.username, ignoreCase = true)) {
                return "You cannot mute yourself"
            }
            try {
                conversationService.muteUserInIrc(username)
            } catch (e: IllegalArgumentException) {
                return e.message
            }
            return "Success"
        } else if (args[0] == "unmute") {
            if (args.size != 2) {
                return "Bad usage. unmute <username>"
            }
            val username = args[1]
            try {
                conversationService.unmuteUserInIrc(username)
            } catch (e: IllegalArgumentException) {
                return e.message
            }
            return "Success"
        }
        return help()
    }
}