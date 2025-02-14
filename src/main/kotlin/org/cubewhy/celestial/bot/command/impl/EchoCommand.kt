package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User

class EchoCommand : Command {
    override val name: String
        get() = "echo"
    override val usage: String
        get() = "<message>"

    override fun process(command: String, sender: User): String {
        return command
    }
}