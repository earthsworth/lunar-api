package org.cubewhy.celestial.bot.command.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class ToggleLunarPlusCommand(
    private val userRepository: UserRepository
) : Command {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun trigger() = "plus"

    override fun usage() = ""

    override fun description() = "Toggle Lunar Plus"

    override suspend fun execute(user: User, args: List<String>): String {
        val newState = !user.cosmetic.lunarPlusState
        logger.info { "User ${user.username} ${if (newState) "enabled" else "disabled"} Lunar+ feature" }
        user.cosmetic.lunarPlusColor = if (newState) PlusColor.AQUA.color else 0
        // save user
        userRepository.save(user).awaitFirst()
        return "Success ${if (newState) "enabled" else "disabled"}. Please restart your client"
    }
}