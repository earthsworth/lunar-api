package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.Instant

@Document
data class User(
    @Id
    val id: String? = null,

    var username: String,
    val uuid: String,
    var role: Role,

    var radioPremium: Boolean = false,
    var createdAt: Instant = Instant.now(),
    var lastSeenAt: Instant = Instant.now(),
    var allowFriendRequests: Boolean = true,
    var lunarPlusColor: Int? = null,

    var cosmetic: UserCosmeticSettings = UserCosmeticSettings(),
    var emote: UserEmoteSettings = UserEmoteSettings()
)

/**
 * A entity to store user sessions between clusters, loadbalancer
 * */
data class OnlineUser(
    var userUuid: String,
    var websocketId: String
): Serializable

data class UserEmoteSettings(
    var equippedEmotes: List<Emote> = mutableListOf()
)

data class UserCosmeticSettings(
    var lunarPlusColor: Int? = null,
    var clothCloak: Boolean = true,
    var flipShoulderPet: Boolean = false,
    var activeCosmetics: List<Int> = mutableListOf(),
    var showHatsOverHelmet: Boolean = false,
    var showHatsOverSkinLayer: Boolean = false,
    var hatHeightOffsetCount: Int = 0,
    var showHatsOverHat: Boolean = false,
    var showOverChestplate: Boolean = false,
    var showOverBoots: Boolean = false,
    var showOverLeggings: Boolean = false,
    var equippedCosmetics: List<UserCosmetic> = emptyList(),
    var logoAlwaysShow: Boolean = true
)