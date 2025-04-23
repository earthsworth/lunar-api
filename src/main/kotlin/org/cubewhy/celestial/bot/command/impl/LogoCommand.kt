package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.LogoColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.findEnumByNameIgnoreCase
import org.springframework.stereotype.Component

@Component
class LogoCommand(private val userService: UserService) : Command {
    override fun trigger() = "logo"

    override fun usage() = "available - get available colors\nset <color> - set available color"

    override fun description() = "Switch your Lunar logo color"

    override suspend fun execute(user: User, args: List<String>): String {
        if (args.isEmpty()) {
            return "Current logo color: ${user.cosmetic.lunarLogoColor.name}"
        }
        return when (args[0]) {
            "available" -> "Available logo colors: ${user.availableLogoColors}"
            "set" -> {
                // get color
                if (args.size != 2) {
                    return "Bad usage: set <color>"
                }
                val colorName = args[1]
                val color = findEnumByNameIgnoreCase<LogoColor>(colorName) ?: return "Bad color"
                try {
                    userService.switchLogoColor(user, color)
                } catch (e: IllegalStateException) {
                    return "Failure: ${e.message}"
                }
                return "Success"
            }

            else -> "Unknown"
        }
    }
}