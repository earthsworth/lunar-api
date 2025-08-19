package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.kotlin.toByteStringUtf8
import com.lunarclient.websocket.cosmetic.v1.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.event.UserSubscribeEvent
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.CosmeticService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.toLunarClientUUID
import org.cubewhy.celestial.util.wrapCommonServer
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.time.Instant
import java.util.*
import com.lunarclient.websocket.cosmetic.v1.LoginRequest as CosmeticsLoginRequest

@Service
class CosmeticServiceImpl(
    private val userRepository: UserRepository,
    private val subscriptionService: SubscriptionService,
    private val sessionService: SessionService,
) : CosmeticService {

    override val serviceName: String = "lunarclient.websocket.cosmetic.v1.CosmeticService"

    companion object {
        private val logger = KotlinLogging.logger {}
    }

//    private val cosmeticList = mutableListOf<Cosmetic>()
//
//    @PostConstruct
//    private fun init() {
//        logger.info { "Loading cosmetics from csv" }
//        val resource = ClassPathResource("cosmetic/index.csv")
//        // load cosmetics
//        resource.inputStream.use { inputStream ->
//            InputStreamReader(inputStream).use { reader ->
//                CSVReader(reader).use { csvReader ->
//                    csvReader.forEach { row ->
//                        if (row.size >= 4) {
//                            val id = row[0].trim().toInt()
//                            val name = row[3].trim()
//                            cosmeticList.add(Cosmetic(id, name))
//                        }
//                    }
//                }
//            }
//        }
//        logger.info { "Loaded ${cosmeticList.size} cosmetics" }
//    }

    override suspend fun refreshCosmetics(user: User) {
        sessionService.push(user, RefreshCosmeticsPush.getDefaultInstance())
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        connection: ClientConnection<*>,
        user: User
    ): RpcResponse {
        return when (method) {
            "Login" -> {
                connection.metadata.upstreamConnection?.let { upstream ->
                    // forward to upstream
                    // NOTE: the payload is empty
                    val payload = CosmeticsLoginRequest.getDefaultInstance()
                        .wrapCommonServer(UUID.randomUUID().toString().toByteStringUtf8(), serviceName, "Login")
                    upstream.send(payload)
                }
                this.processLogin(user).toWebsocketResponse()
            } // process login packet
            "UpdateCosmeticSettings" -> {
                // parse payload
                val pb = UpdateCosmeticSettingsRequest.parseFrom(payload)
                this.processUpdateCosmeticSettings(pb, user, connection).toWebsocketResponse()
            }

            else -> emptyWebsocketResponse() // unknown packet
        }
    }

    override suspend fun processUpdateCosmeticSettings(
        message: UpdateCosmeticSettingsRequest,
        user: User,
        connection: ClientConnection<*>
    ): GeneratedMessage {
        user.cosmetic.equippedCosmetics =
            message.settings.equippedCosmeticsList.map { UserCosmetic(it.cosmeticId, Instant.now(), null, null) }
        user.cosmetic.flipShoulderPet = message.settings.flipShoulderPet
        user.cosmetic.lunarPlusColor = PlusColor.entries.firstOrNull { it.color == message.settings.plusColor.color }?: PlusColor.GREEN
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
        subscriptionService.getWorldPlayerUuids(connection)
            .forEach { uuid ->
                userRepository.findByUuid(uuid).awaitFirstOrNull()?.let {
                    this.pushCosmeticEvent(user, message.settings)
                }
            }
        return UpdateCosmeticSettingsResponse.getDefaultInstance()
    }

    private suspend fun pushCosmeticEvent(
        user: User,
        settings: CustomizableCosmeticSettings
    ) {
        // push cosmetics event
        sessionService.push(user, this@CosmeticServiceImpl.buildCosmeticsPush(user, settings))
    }

    private fun buildCosmeticsPush(
        user: User,
        settings: CustomizableCosmeticSettings
    ) =
        PlayerCosmeticsPush.newBuilder().apply {
            this.playerUuid = user.uuid.toLunarClientUUID()
            this.settings = settings
            this.logoColor = user.logoColor
            this.logoAlwaysShow = user.cosmetic.logoAlwaysShow
        }.build()

    override suspend fun processLogin(user: User): GeneratedMessage {
        return LoginResponse.newBuilder().apply {
            settings = buildCosmeticSettings(user)
            logoColor = user.logoColor
            rankName = "Player"
            if (user.cosmetic.lunarPlusState) {
                addAllAvailableLunarPlusColors(PlusColor.entries.map { it.toLunarClientColor() })
            }
//            addAllOwnedCosmeticIds(cosmeticList.map { it.cosmeticId })
//            addAllOwnedCosmetics(cosmeticList.map { it.toUserCosmetic().toOwnedCosmetic() })
            logoAlwaysShow = user.cosmetic.logoAlwaysShow
            // hack: use LunarClient's hasAllCosmeticsFlag
            hasAllCosmeticsFlag = true
        }.build()
    }

    private fun buildCosmeticSettings(user: User): CustomizableCosmeticSettings {
        return CustomizableCosmeticSettings.newBuilder().also { builder ->
            builder.addAllActiveCosmeticIds(user.cosmetic.activeCosmetics.map { it })
            builder.addAllEquippedCosmetics(user.cosmetic.equippedCosmetics.map { it.toEquippedCosmetic() })
            builder.flipShoulderPet = user.cosmetic.flipShoulderPet
            builder.showHatsOverHelmet = user.cosmetic.showHatsOverHelmet
            builder.showHatsOverSkinLayer = user.cosmetic.showHatsOverSkinLayer
            builder.showOverChestplate = user.cosmetic.showOverChestplate
            builder.showOverLeggings = user.cosmetic.showOverLeggings
            builder.showOverBoots = user.cosmetic.showOverBoots
            if (user.cosmetic.lunarPlusState) {
                builder.clothCloak = user.cosmetic.clothCloak
                builder.plusColor = (user.cosmetic.lunarPlusColor ?: PlusColor.GREEN).toLunarClientColor()
            }
        }.build()
    }

    @EventListener
    fun onUserSubscribe(event: UserSubscribeEvent): Mono<Void> {
        // push other user's cosmetic data to user
        logger.debug { "Sync multiplayer cosmetics data for user ${event.user.username}" }
        return userRepository.findAllByUuidIn(event.uuids.toFlux())
            .flatMap { user ->
                val push = this@CosmeticServiceImpl.buildCosmeticsPush(user, buildCosmeticSettings(user))
                mono {
                    // push to websocket
                    event.connection.sendPush(push)
                }
            }.then()
    }
}