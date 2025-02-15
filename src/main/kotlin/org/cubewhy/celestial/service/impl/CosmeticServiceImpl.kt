package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1
import com.opencsv.CSVReader
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.event.UserSubscribeEvent
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CosmeticService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.pushEvent
import org.cubewhy.celestial.util.toLunarClientColor
import org.cubewhy.celestial.util.toLunarClientUUID
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.io.InputStreamReader
import java.time.Instant

@Service
class CosmeticServiceImpl(
    private val userRepository: UserRepository,
    private val subscriptionService: SubscriptionService,
    private val sessionService: SessionService,
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
                        if (row.size >= 4) {
                            val id = row[0].trim().toInt()
                            val name = row[3].trim()
                            cosmeticList.add(Cosmetic(id, name))
                        }
                    }
                }
            }
        }
        logger.info { "Loaded ${cosmeticList.size} cosmetics" }
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "Login" -> this.processLogin(user).toWebsocketResponse() // process login packet
            "UpdateCosmeticSettings" -> {
                // parse payload
                val pb = WebsocketCosmeticV1.UpdateCosmeticSettingsRequest.parseFrom(payload)
                this.processUpdateCosmeticSettings(pb, user, session).toWebsocketResponse()
            }

            else -> emptyWebsocketResponse() // unknown packet
        }
    }

    override suspend fun processUpdateCosmeticSettings(
        message: WebsocketCosmeticV1.UpdateCosmeticSettingsRequest,
        user: User,
        session: WebSocketSession
    ): GeneratedMessage {
        user.cosmetic.equippedCosmetics =
            message.settings.equippedCosmeticsList.map { UserCosmetic(it.cosmeticId, Instant.now(), null, null) }
        user.cosmetic.flipShoulderPet = message.settings.flipShoulderPet
        user.cosmetic.lunarPlusColor = message.settings.plusColor.color
        user.cosmetic.clothCloak = message.settings.clothCloak
        user.cosmetic.showHatsOverHelmet = message.settings.showHatsOverHelmet
        user.cosmetic.showHatsOverSkinLayer = message.settings.showHatsOverSkinLayer
        user.cosmetic.hatHeightOffsetCount = message.settings.hatHeightOffsetCount
        user.cosmetic.showOverChestplate = message.settings.showOverChestplate
        user.cosmetic.showOverBoots = message.settings.showOverBoots
        user.cosmetic.showOverLeggings = message.settings.showOverLeggings
        // save user
        logger.info { "Saving cosmetics settings of user ${user.username} (count: ${message.settings.equippedCosmeticsList.size})" }
        userRepository.save(user).awaitFirst()
        // push settings to other players
        subscriptionService.getWorldPlayerUuids(session)
            .forEach { uuid ->
                val targetSession = sessionService.getSession(uuid)
                // push cosmetics event
                targetSession?.pushCosmeticEvent(user, message.settings)
            }
        return WebsocketCosmeticV1.UpdateCosmeticSettingsResponse.getDefaultInstance()
    }

    private suspend fun WebSocketSession.pushCosmeticEvent(
        user: User,
        settings: WebsocketCosmeticV1.CustomizableCosmeticSettings
    ) {
        // push cosmetics event
        this.pushEvent(this@CosmeticServiceImpl.buildCosmeticsPush(user, settings))
    }

    private fun buildCosmeticsPush(
        user: User,
        settings: WebsocketCosmeticV1.CustomizableCosmeticSettings
    ) =
        WebsocketCosmeticV1.PlayerCosmeticsPush.newBuilder().apply {
            this.playerUuid = user.uuid.toLunarClientUUID()
            this.settings = settings
            this.logoColor = user.role.toLunarClientColor()
            this.logoAlwaysShow = user.cosmetic.logoAlwaysShow
        }.build()

    override suspend fun processLogin(user: User): GeneratedMessage {
        return WebsocketCosmeticV1.LoginResponse.newBuilder().apply {
            settings = buildCosmeticSettings(user)
            logoColor = user.role.toLunarClientColor()
            rankName = user.role.rank
            if (user.cosmetic.lunarPlusState) {
                addAllAvailableLunarPlusColors(PlusColor.entries.map { it.toLunarClientColor() })
            }
            addAllOwnedCosmeticIds(cosmeticList.map { it.cosmeticId })
            addAllOwnedCosmetics(cosmeticList.map { it.toUserCosmetic().toOwnedCosmetic() })
            logoAlwaysShow = user.cosmetic.logoAlwaysShow
            hasAllCosmeticsFlag = true
        }.build()
    }

    private fun buildCosmeticSettings(user: User): WebsocketCosmeticV1.CustomizableCosmeticSettings {
        return WebsocketCosmeticV1.CustomizableCosmeticSettings.newBuilder().apply {
            addAllActiveCosmeticIds(user.cosmetic.activeCosmetics.map { it })
            addAllEquippedCosmetics(user.cosmetic.equippedCosmetics.map { it.toEquippedCosmetic() })
            flipShoulderPet = user.cosmetic.flipShoulderPet
            showHatsOverHelmet = user.cosmetic.showHatsOverHelmet
            showHatsOverSkinLayer = user.cosmetic.showHatsOverSkinLayer
            showOverChestplate = user.cosmetic.showOverChestplate
            showOverLeggings = user.cosmetic.showOverLeggings
            showOverBoots = user.cosmetic.showOverBoots
            if (user.cosmetic.lunarPlusState) {
                clothCloak = user.cosmetic.clothCloak
                plusColor = user.cosmetic.lunarPlusColor.toLunarClientColor()
            }
        }.build()
    }

    @EventListener
    fun onUserSubscribe(event: UserSubscribeEvent): Mono<Void> {
        // push other user's cosmetic data to user
        logger.info { "Sync multiplayer cosmetics data for user ${event.user.username}" }
        return userRepository.findAllByUuidIn(event.uuids.toFlux())
            .flatMap { user ->
                val push = this@CosmeticServiceImpl.buildCosmeticsPush(user, buildCosmeticSettings(user))
                mono {
                    // push to websocket
                    event.session.pushEvent(push)
                }
            }.then()
    }
}
