package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.Color


enum class Role(val color: Int, val rank: String) {
    USER(0xFFFFFF, "User"),
    STAFF(0x2EE101, "Staff"),
    OWNER(0x96010E, "Owner"),
    ADMIN(0xE10110, "Admin"),
    DEVELOPER(0x00FFF0, "Dev"),
    YELLOW_FISH(0xEEC42A, "YellowFish"),
    SUPPORTER(0xE17E2E, "Supporter"),
    PARTNER(0xFCAC04, "Partner");

    fun toLunarClientColor(): Color = Color.newBuilder()
        .setColor(this.color)
        .build()
}