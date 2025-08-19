package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.UserService
import org.springframework.stereotype.Component

@Component
class ResetProfileCommand(
    private val userService: UserService
) : Command {
    override fun trigger(): String = "resetprofile"

    override fun usage(): String = ""

    override fun description(): String = "Reset everything expect your role for your profile"

    override suspend fun execute(user: User, args: List<String>): String {
        userService.resetProfile(user)
        return "Completed"
    }
}