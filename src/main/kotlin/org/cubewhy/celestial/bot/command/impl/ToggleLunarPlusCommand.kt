package org.cubewhy.celestial.bot.command.impl

import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.handler.getSessionLocally
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.util.wrapPush
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toMono

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
        user.cosmetic.lunarPlusColor = if (newState) PlusColor.AQUA.color else PlusColor.NONE.color
        // save user
        userRepository.save(user).awaitFirst()

        getSessionLocally(user.uuid)?.let { session ->
            session.send(session.binaryMessage{
                it.wrap(WebsocketCosmeticV1.RefreshCosmeticsPush.getDefaultInstance().wrapPush().toByteArray())
            }.toMono())
        }

        return "Success ${if (newState) "enabled" else "disabled"}."
    }
}