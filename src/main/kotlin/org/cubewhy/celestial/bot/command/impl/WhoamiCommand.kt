package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.springframework.stereotype.Component

@Component
class WhoamiCommand : Command {
    override fun trigger() = "whoami"

    override fun usage() = ""

    override fun description() = "Display your login information"

    override suspend fun execute(user: User, args: List<String>): String {
        return "name: ${user.username}, uuid: ${user.uuid}"
    }
}