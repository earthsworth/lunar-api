package org.cubewhy.celestial.bot.command.impl

import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.bot.command.Command
import org.cubewhy.celestial.entity.SavedCosmetics
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.SavedCosmeticsRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CosmeticService
import org.springframework.stereotype.Component

@Component
class CosmeticCommand(
    private val savedCosmeticsRepository: SavedCosmeticsRepository,
    private val userRepository: UserRepository,
    private val cosmeticService: CosmeticService
) : Command {
    override fun trigger(): String = "cosmetic"

    override fun description(): String = "Manager your cosmetic"

    override fun usage(): String = "cosmetic <save/load> <id>"

    override suspend fun execute(user: User, args: List<String>): String {
        return when (args[0]) {
            "save" -> {
                this.saveCosmetic(user)
            }

            "load" -> {
                this.loadCosmetics(user, args[0])
            }

            else -> this.usage()
        }
    }

    private suspend fun saveCosmetic(user: User): String {
        val index = savedCosmeticsRepository.count().awaitFirst() + 1
        savedCosmeticsRepository.save(SavedCosmetics(null, user.cosmetic)).awaitFirst()
        return "Saved current Cosmetics! id: $index"
    }

    private suspend fun loadCosmetics(user: User, id: String): String {
        val cosmetics = savedCosmeticsRepository.findById(id).awaitFirst()
        user.cosmetic = cosmetics.cosmetics
        userRepository.save(user).awaitFirst()
        cosmeticService.refreshCosmetics(user)
        return "Loaded Cosmetics from $id"
    }
}