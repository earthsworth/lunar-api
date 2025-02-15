package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.springframework.stereotype.Component

@Component
class EchoCommand : Command {
    override fun trigger() = "echo"
    override fun usage() = "<text>"
    override fun description() = "Display a line of text"

    override suspend fun execute(user: User, args: List<String>): String {
        if (args.isEmpty()) {
            return help()
        }
        return args.joinToString(" ")
    }
}