package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.LunarclientCommonV1.UuidAndUsername
import org.cubewhy.celestial.util.toLunarClientUUID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.Instant

/**
 * The web dashboard user
 * */
@Document
data class WebUser(
    @Id
    val id: String? = null,
    var username: String,
    var password: String, // encrypted password
    var gameUser: String? = null, // bind to in-game user id

    var role: Role = Role.USER,
    val createdAt: Instant = Instant.now(),
)

/**
 * The in-game user
 * */
@Document
data class User(
    @Id
    val id: String? = null,

    var username: String,
    val uuid: String,
    var role: Role, // synced with web user

    var radioPremium: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var lastSeenAt: Instant = Instant.now(),
    var allowFriendRequests: Boolean = true,

    var status: UserStatus = UserStatus.ONLINE,

    var cosmetic: UserCosmeticSettings = UserCosmeticSettings(),
    var emote: UserEmoteSettings = UserEmoteSettings()
) {
    fun toLunarClientPlayer(): UuidAndUsername = UuidAndUsername.newBuilder().apply {
        this.uuid = this@User.uuid.toLunarClientUUID()
        this.username = this@User.username
    }.build()
}

/**
 * An entity to store user sessions among clusters, loadbalancer
 * */
data class OnlineUser(
    var userUuid: String,
    var websocketId: String,
    var location: String? = null,
    var minecraftVersion: String? = null
) : Serializable

data class UserEmoteSettings(
    var equippedEmotes: List<Emote> = mutableListOf()
)

data class UserCosmeticSettings(
    var lunarPlusColor: Int = 0,
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
) {
    val lunarPlusState: Boolean
        get() {
            return lunarPlusColor > 0
        }
}