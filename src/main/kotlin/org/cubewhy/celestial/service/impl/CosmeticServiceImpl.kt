package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1
import com.opencsv.CSVReader
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.cubewhy.celestial.entity.Cosmetic
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.CosmeticService
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.io.InputStreamReader

@Service
class CosmeticServiceImpl : CosmeticService {

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
                return this.processLogin(user)
            }

            else -> {
                // unknown packet
                return null
            }
        }
    }

    private suspend fun processLogin(user: User): GeneratedMessage {
        val builder = WebsocketCosmeticV1.LoginResponse.newBuilder()
            .setSettings(this.buildCosmeticSettings(user))
            .setLogoColor(user.role.toLunarClientColor())
            .setRankName(user.role.rank)
            .addAllAvailableLunarPlusColors(PlusColor.entries.map { i -> i.toLunarClientColor() }.toList())
            .addAllOwnedCosmetics(cosmeticList.map { it.toUserCosmetic().toOwnedCosmetic() }.toList())
            .setLogoAlwaysShow(true)
            .setHasAllCosmeticsFlag(true)
        return builder.build()
    }

    private fun buildCosmeticSettings(user: User): WebsocketCosmeticV1.CustomizableCosmeticSettings {
        val builder = WebsocketCosmeticV1.CustomizableCosmeticSettings.newBuilder()
            .setClothCloak(user.clothCloak)
            .addAllActiveCosmeticIds(user.activeCosmetics.map { it.cosmeticId }.toList())
            .addAllEquippedCosmetics(user.equippedCosmetics.map { it.toEquippedCosmetic() }.toList())
            .setFlipShoulderPet(false)
            .setPlusColor(user.lunarPlusColor?.toLunarClientColor())
        return builder.build()
    }
}
