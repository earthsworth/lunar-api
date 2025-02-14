package org.cubewhy.celestial.bot.command

import org.cubewhy.celestial.bot.command.impl.EchoCommand
import org.cubewhy.celestial.entity.User
import org.springframework.stereotype.Service

@Service
class CommandManager {
    private val commands = ArrayList<Command>()

    init {
        commands.add(EchoCommand())
    }

    fun process(command: String, sender: User): String {
        commands.forEach {
            if (it.name == command.split(" ")[0]) {
                return it.process(command.substring(command.split(" ")[0].length - 1), sender)
            }
        }
        return "Unknown command: $command"
    }
}