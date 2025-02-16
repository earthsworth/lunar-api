package org.cubewhy.celestial.bot.command.impl

import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.util.pushEvent
import org.springframework.stereotype.Component

@Component
class ToggleLunarPlusCommand(
    private val userRepository: UserRepository,
    private val sessionService: SessionService
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
        user.cosmetic.lunarPlusColor = if (newState) PlusColor.AQUA.color else PlusColor.NONE.color
        // save user
        userRepository.save(user).awaitFirst()

        // push event
        sessionService.getSession(user)?.pushEvent(WebsocketCosmeticV1.RefreshCosmeticsPush.getDefaultInstance())
        return "Success ${if (newState) "enabled" else "disabled"} Lunar+ feature."
    }
}