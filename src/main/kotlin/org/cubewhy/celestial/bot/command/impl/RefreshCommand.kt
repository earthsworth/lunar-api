package org.cubewhy.celestial.bot.command.impl

import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.CosmeticService
import org.cubewhy.celestial.service.EmoteService
import org.springframework.stereotype.Component

@Component
class RefreshCommand(
    private val cosmeticService: CosmeticService,
    private val emoteService: EmoteService
) : Command {
    override fun trigger(): String = "refresh"

    override fun usage(): String = ""

    override fun description(): String = "Refresh command"

    override suspend fun execute(user: User, args: List<String>): String {
        cosmeticService.refreshCosmetics(user)
        emoteService.refreshEmote(user)
        return "Successfully refreshed!"
    }
}