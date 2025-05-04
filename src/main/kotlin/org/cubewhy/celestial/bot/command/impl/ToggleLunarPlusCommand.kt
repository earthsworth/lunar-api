package org.cubewhy.celestial.bot.command.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CosmeticService
import org.springframework.stereotype.Component

@Component
class ToggleLunarPlusCommand(
    private val userRepository: UserRepository,
    private val cosmeticService: CosmeticService
) : Command {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun trigger() = "plus"

    override fun usage() = ""

    override fun description() = "Toggle Lunar+ features"

    override suspend fun execute(user: User, args: List<String>): String {
        val oldState = user.cosmetic.lunarPlusState
        logger.debug { "User ${user.username} ${if (oldState) "enabled" else "disabled"} Lunar+ feature" }
        user.cosmetic.lunarPlusState = !user.cosmetic.lunarPlusState
        // save user
        userRepository.save(user).awaitFirst()

        // push event
        cosmeticService.refreshCosmetics(user)
        return "Success ${if (!oldState) "enabled" else "disabled"} Lunar+ feature."
    }
}