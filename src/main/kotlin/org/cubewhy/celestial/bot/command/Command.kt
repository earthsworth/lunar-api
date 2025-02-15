package org.cubewhy.celestial.bot.command

import org.cubewhy.celestial.entity.User

interface Command {
    fun trigger(): String
    fun usage(): String
    fun description(): String

    fun help() = "${trigger()} - ${description()}\n${usage()}"

    suspend fun execute(user: User, args: List<String>): String
}