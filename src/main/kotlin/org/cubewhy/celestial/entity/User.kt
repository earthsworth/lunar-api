package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.Color
import com.lunarclient.common.v1.UuidAndUsername
import org.cubewhy.celestial.util.toLunarClientColor
import org.cubewhy.celestial.util.toLunarClientUUID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.Instant

/**
 * The user
 * */
@Document
data class User(
    @Id
    val id: String? = null,

    var username: String,
    var password: String? = null,
    @Indexed(unique = true)
    val uuid: String,
    val roles: MutableList<Role> = mutableListOf(Role.USER),

    var radioPremium: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var lastSeenAt: Instant = Instant.now(),
    var allowFriendRequests: Boolean = true,
    var pinFriends: List<String> = mutableListOf(),

    var status: UserStatus = UserStatus.ONLINE,

    var cosmetic: UserCosmeticSettings = UserCosmeticSettings(),
    var emote: UserEmoteSettings = UserEmoteSettings(),
) {
    fun toLunarClientPlayer(): UuidAndUsername = UuidAndUsername.newBuilder().apply {
        this.uuid = this@User.uuid.toLunarClientUUID()
        this.username = this@User.username
    }.build()

    val logoColor: Color
        get() {
            // lunar+
            if (this.cosmetic.lunarPlusState && this.cosmetic.lunarLogoColor == LogoColor.WHITE) return LogoColor.PINK.color.toLunarClientColor()
            return this.cosmetic.lunarLogoColor.color.toLunarClientColor()
        }

    val availableLogoColors: Set<LogoColor>
        get() = this.roles.flatMap { it.availableLogoColors.toList() }.toSet()
}

/**
 * An entity to store user sessions among clusters, loadbalancer
 * */
data class UserWebsocketSession(
    var userId: String,
    var userUuid: String,
    var websocketId: String,
    var location: String? = null,
    var minecraftVersion: String? = null
) : Serializable

data class UserEmoteSettings(
    var equippedEmotes: List<Emote> = mutableListOf()
)

data class UserCosmeticSettings(
    var lunarLogoColor: LogoColor = LogoColor.WHITE,
    var lunarPlusColor: PlusColor = PlusColor.GREEN,
    var lunarPlusState: Boolean = true,
    var clothCloak: Boolean = true,

    var flipShoulderPet: Boolean = false,
    var activeCosmetics: List<Int> = mutableListOf(),
    var showHatsOverHelmet: Boolean = false,
    var showHatsOverSkinLayer: Boolean = false,
    var hatHeightOffsetCount: Int = 0,
    var showOverChestplate: Boolean = false,
    var showOverBoots: Boolean = false,
    var showOverLeggings: Boolean = false,
    var equippedCosmetics: List<UserCosmetic> = emptyList(),
    var logoAlwaysShow: Boolean = true
)

