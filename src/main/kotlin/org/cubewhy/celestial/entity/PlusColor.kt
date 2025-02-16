package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.LunarclientCommonV1

enum class PlusColor(val color: Int) {
    NONE(-1),
    PURPLE(0x7F00D9),
    AMBER(0xFCB423),
    AQUA(0x00FFF0),
    RED(0xE10049),
    BLUE(0x1981C8);

    fun toLunarClientColor(): LunarclientCommonV1.Color = LunarclientCommonV1.Color.newBuilder()
        .setColor(this.color)
        .build()

}