package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.bot.command.impl.EchoCommand
import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.CommandService
import org.springframework.stereotype.Service

@Service
class CommandServiceImpl(
    echoCommand: EchoCommand
) : CommandService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val commands = mutableMapOf<String, Command>()

    init {
        val commandList = listOf<Command>(
            echoCommand
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

        val rawResponse = commands[command] ?.execute(user, args) ?: return "Unknown command, type .help for help"
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
            return joinHelpMessage()
        } else if (args.size == 1) {
            val command = commands[args[0]] ?: return "Unknown command"
            return ".${command.trigger()} ${command.usage()} - ${command.description()}"
        }
        return "Unknown usage"
    }

    private fun joinHelpMessage(): String {
        val sb = StringBuilder()
        sb.append("Welcome to LunarCN! Yet another LunarClient API implementation\n")
            .append("use .help [command] to display the full information of a command\n")
        commands.values.forEach { command ->
            sb.append(".${command.trigger()} ${command.description()}")
                .append("\n")
        }
        sb.append("Have fun with cosmetics!")
        return sb.toString()
    }
}