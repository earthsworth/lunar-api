package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.UserService
import org.springframework.stereotype.Component

@Component
class PasswordCommand(private val userService: UserService) : Command {
    override fun trigger() = "passwd"

    override fun usage() = "<new password>"

    override fun description() = "Select a password for the dashboard"

    override suspend fun execute(user: User, args: List<String>): String {
        if (args.isEmpty()) {
            return help()
        }
        val password = args[0]
        userService.updatePassword(user, password)
        return "Success"
    }
}