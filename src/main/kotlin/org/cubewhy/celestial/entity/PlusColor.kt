package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.Color

enum class PlusColor(val color: Int) {
    GREEN(0x00E519),
    YELLOW(0xFFFF55),
    AQUA(0x55FFFF),
    GOLD(0xFFAA00),
    BLUE(0x5555FF),
    PINK(0xFF55FF),
    CYAN(0x00AAAA),
    LIME(0x008000),
    MAGENTA(0xFD349C),
    MAROON(0xBC243C),
    SILVER(0xFFFFFF),
    TEAL(0xC4FC18),
    NAVY(0x111174),
    ORANGE(0xFC3D03),
    CORAL(0xFF8AFF),
    BROWN(0x964B00),
    CRIMSON(0xDC143C),
    PURPLE(0x7F00D9),
    AMBER(0xc0e0a4),
    TURQUOISE(0xFFD7BE),
    COPPER(0xFF9900),
    SALMON(0xFA8072),
    VIOLET(0x800080),
    CHARCOAL(0x333333),
    MINT(0xB2FFFC),
    LAVENDER(0xC7B8EA),
    PEACH(0x808080),
    GREY(0x000000),
    RED(0x730505);

    fun toLunarClientColor(): Color = Color.newBuilder()
        .setColor(this.color)
        .build()

}