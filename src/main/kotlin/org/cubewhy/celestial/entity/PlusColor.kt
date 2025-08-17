package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.Color

enum class PlusColor(val color: Int) {
    GREEN(0x16B82E),
    YELLOW(0xC9CA58),
    AQUA(0x52CACE),
    GOLD(0xC98F1C),
    BLUE(0x5254CE),
    PINK(0xC954CE),
    CYAN(0x178F93),
    LIME(0x17721C),
    MAGENTA(0xC73D89),
    MAROON(0x9A3246),
    SILVER(0xC9CACE),
    TEAL(0x9FC82D),
    NAVY(0x23246D),
    ORANGE(0xC7431E),
    CORAL(0xC979CE),
    RED(0xE10049),
    CRIMSON(0xDC143C),
    PURPLE(0x7F00D9),
    AMBER(0xFCB423),
    TURQUOISE(0x1ABC9C),
    COPPER(0xFF9900),
    SALMON(0xFA8072),
    VIOLET(0x800080),
    CHARCOAL(0x333333),
    MINT(0xB2FFFC),
    LAVENDER(0xC7B8EA),
    PEACH(0xFFD7BE),
    GREY(0x808080),
    BROWN(0x964B00);

    fun toLunarClientColor(): Color = Color.newBuilder()
        .setColor(this.color)
        .build()

}