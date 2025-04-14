package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.Message
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.CommandService
import org.cubewhy.celestial.service.ConversationService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class CommandServiceImpl(
    commandList: MutableList<out Command>,
    @Lazy
    private val conversationService: ConversationService
) : CommandService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val commands = mutableMapOf<String, Command>()

    init {
        commandList.forEach {
            commands[it.trigger()] = it
        }
        // add the help command
        val helpCommand = HelpCommand(commands)
        commands[helpCommand.trigger()] = helpCommand
    }

    override suspend fun process(message: String, user: User): Message? {
        if (!message.startsWith(".")) {
            // irc message
            if (message.startsWith("passwd ") || message.startsWith("/passwd")) {
                return Message.createBotResponse("Message blocked! Use .passwd to set a password", user)
            }
            conversationService.pushIrc(user.username, message, user, fromDiscord = false)
            return null
        }
        // parse command
        val response = parseCommand(user, message) ?: return null
        return Message.createBotResponse(response, user)
    }

    suspend fun parseCommand(user: User, message: String): String? {
        val parts = message.split(" ")
        val command = parts[0].substring(1)
        val args = parts.drop(1)

        val command1 = commands[command]?: return "Unknown command, type .help for help"
        // check permission
        val trigger = command1.trigger()
        val requiredRoles = command1.roles()
        if (!(requiredRoles.isEmpty() || requiredRoles.toSet().intersect(user.roles.toSet()).isNotEmpty())) {
            return "[$trigger] You have no enough permission to use this command"
        }
        logger.info { "User ${user.username} executes command $trigger" }
        val rawResponse = command1.execute(user, args)
        if (rawResponse.isNullOrEmpty()) return null
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
            val requiredRoles = command.roles()
            if (requiredRoles.isEmpty() || requiredRoles.toSet().intersect(user.resolvedRoles.toSet()).isNotEmpty()) {
                sb.append(".${command.trigger()} ${command.description()}")
                    .append("\n")
            }
        }
        sb.append("Have fun with cosmetics!")
        return sb.toString()
    }
}