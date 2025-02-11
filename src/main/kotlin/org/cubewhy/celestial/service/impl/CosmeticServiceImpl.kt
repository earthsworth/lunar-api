package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1
import com.opencsv.CSVReader
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Cosmetic
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CosmeticService
import org.cubewhy.celestial.util.toLunarClientColor
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.io.InputStreamReader

@Service
class CosmeticServiceImpl(
    private val userRepository: UserRepository,
) : CosmeticService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val cosmeticList = mutableListOf<Cosmetic>()

    @PostConstruct
    private fun init() {
        logger.info { "Loading cosmetics from csv" }
        val resource = ClassPathResource("cosmetic/index.csv")
        // load cosmetics
        resource.inputStream.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                CSVReader(reader).use { csvReader ->
                    csvReader.forEach { row ->
                        if (row.size >= 4) { // 确保至少有 4 个字段
                            val id = row[0].trim().toInt()
                            val name = row[3].trim()
                            cosmeticList.add(Cosmetic(id, name))
                        }
                    }
                }
            }
        }
        logger.info { "Loaded ${cosmeticList.size} cosmetics' data" }
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        when (method) {
            "Login" -> {
                // process login packet
                return this.processLogin(user)
            }

            "UpdateCosmeticSettings" -> {
                // parse payload
                val pb = WebsocketCosmeticV1.UpdateCosmeticSettingsRequest.parseFrom(payload)
                return this.updateCosmeticSettings(pb, user)
            }

            else -> {
                // unknown packet
                return null
            }
        }
    }

    private suspend fun updateCosmeticSettings(
        message: WebsocketCosmeticV1.UpdateCosmeticSettingsRequest,
        user: User
    ): GeneratedMessage? {
        user.cosmetic.clothCloak = message.settings.clothCloak
        user.cosmetic.activeCosmetics = message.settings.activeCosmeticIdsList
        user.cosmetic.flipShoulderPet = message.settings.flipShoulderPet
        user.cosmetic.lunarPlusColor = message.settings.plusColor.color
        user.cosmetic.showHatsOverHelmet = message.settings.showHatsOverHelmet
        user.cosmetic.showHatsOverSkinLayer = message.settings.showHatsOverSkinLayer
        user.cosmetic.hatHeightOffsetCount = message.settings.hatHeightOffsetCount
        user.cosmetic.showOverChestplate = message.settings.showOverChestplate
        user.cosmetic.showOverBoots = message.settings.showOverBoots
        user.cosmetic.showOverLeggings = message.settings.showOverLeggings
        // save user
        userRepository.save(user).awaitFirst()
        return WebsocketCosmeticV1.UpdateCosmeticSettingsResponse.getDefaultInstance()
    }

    private suspend fun processLogin(user: User): GeneratedMessage {
        return WebsocketCosmeticV1.LoginResponse.newBuilder().apply {
            settings = buildCosmeticSettings(user)
            logoColor = user.role.toLunarClientColor()
            rankName = user.role.rank
            addAllAvailableLunarPlusColors(PlusColor.entries.map { it.toLunarClientColor() })
            addAllOwnedCosmeticIds(cosmeticList.map { it.cosmeticId })
            addAllOwnedCosmetics(cosmeticList.map { it.toUserCosmetic().toOwnedCosmetic() })
            logoAlwaysShow = true
            hasAllCosmeticsFlag = true
        }.build()
    }

    private fun buildCosmeticSettings(user: User): WebsocketCosmeticV1.CustomizableCosmeticSettings {
        return WebsocketCosmeticV1.CustomizableCosmeticSettings.newBuilder().apply {
            clothCloak = user.cosmetic.clothCloak
            addAllActiveCosmeticIds(user.cosmetic.activeCosmetics.map { it })
//            addAllEquippedCosmetics(cosmeticList.map { it.toUserCosmetic().toEquippedCosmetic() })
            flipShoulderPet = false
            user.cosmetic.lunarPlusColor?.let { setPlusColor(it.toLunarClientColor()) }
        }.build()
    }
}
