package org.cubewhy.celestial.bot.command

import org.cubewhy.celestial.entity.User

interface Command {
    val name: String

    /**
     * @sample <user> (user)
     */
    val usage: String

    /**
     * process command
     * @param command full command
     */
    fun process(command: String, sender: User): String
}