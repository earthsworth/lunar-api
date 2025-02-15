package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.bot.command.impl.EchoCommand
import org.cubewhy.celestial.bot.command.impl.ShoutCommand
import org.cubewhy.celestial.bot.command.impl.WhoamiCommand
import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.CommandService
import org.springframework.stereotype.Service

@Service
class CommandServiceImpl(
    echoCommand: EchoCommand,
    shoutCommand: ShoutCommand,
    whoamiCommand: WhoamiCommand,
) : CommandService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val commands = mutableMapOf<String, Command>()

    init {
        // commands list
        val commandList = listOf(
            echoCommand,
            shoutCommand,
            whoamiCommand
        )

        commandList.forEach {
            commands[it.trigger()] = it
        }
        // add the help command
        val helpCommand = HelpCommand(commands)
        commands[helpCommand.trigger()] = helpCommand
    }

    override suspend fun process(message: String, user: User): Message {
        // parse command
        if (!message.startsWith(".")) {
            return Message.createBotResponse("Not a command, type .help for help", user)
        }
        return Message.createBotResponse(parseCommand(user, message), user)
    }

    suspend fun parseCommand(user: User, message: String): String {
        val parts = message.split(" ")
        val command = parts[0].substring(1)
        val args = parts.drop(1)

        val command1 = commands[command]?: return "Unknown command, type .help for help"
        // check permission
        val trigger = command1.trigger()
        if (!command1.roles().contains(user.role)) {
            return "[$trigger] You have no enough permission to use this command"
        }
        logger.info { "User ${user.username} executes command $trigger" }
        val rawResponse = command1.execute(user, args)
        val sb = StringBuilder()
        // format result
        rawResponse.split("\n").let {
            it.forEachIndexed { index, line ->
                sb.append("[${command}] ").append(line)
                if (index < it.size - 1) {
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }
}

class HelpCommand(private val commands: Map<String, Command>) : Command {
    override fun trigger() = "help"
    override fun usage() = "[command]"
    override fun description() = "Display a list of commands"

    override suspend fun execute(user: User, args: List<String>): String {
        if (args.isEmpty()) {
            // return all help message
            return joinHelpMessage(user)
        } else if (args.size == 1) {
            val command = commands[args[0]] ?: return "Unknown command"
            return ".${command.trigger()} ${command.usage()} - ${command.description()}"
        }
        return "Unknown usage"
    }

    private fun joinHelpMessage(user: User): String {
        val sb = StringBuilder()
        sb.append("Welcome to LunarCN! Yet another LunarClient API implementation\n")
            .append("use .help [command] to display the full information of a command\n")
        commands.values.forEach { command ->
            if (command.roles().contains(user.role)) {
                sb.append(".${command.trigger()} ${command.description()}")
                    .append("\n")
            }
        }
        sb.append("Have fun with cosmetics!")
        return sb.toString()
    }
}