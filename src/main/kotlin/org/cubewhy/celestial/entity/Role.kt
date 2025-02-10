package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.LunarclientCommonV1

enum class Role(val color: Int, val rank: String) {
    USER(0xFFFFFF, "User"),
    STAFF(0x2EE101, "Staff"),
    ADMIN(0xE10110, "Admin"),
    DEVELOPER(0x00FFF0, "Dev"),
    OWNER(0x96010E, "Owner");

    fun toLunarClientColor(): LunarclientCommonV1.Color = LunarclientCommonV1.Color.newBuilder()
        .setColor(this.color)
        .build()
}