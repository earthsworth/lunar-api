package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
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

    var cosmetic: UserCosmeticSettings = UserCosmeticSettings()
) {
}

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
    var equippedCosmetics: List<UserCosmetic> = emptyList()
)