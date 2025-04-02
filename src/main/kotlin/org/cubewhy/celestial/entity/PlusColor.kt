package org.cubewhy.celestial.entity

import com.lunarclient.common.v1.Color

enum class PlusColor(val color: Int) {
    PURPLE(0x7F00D9),
    AMBER(0xFCB423),
    AQUA(0x00FFF0),
    RED(0xE10049),
    BLUE(0x1981C8),
    GREEN(0x3BBE54),
    YELLOW(0xFFFF00),
    ORANGE(0xFFA500),
    PINK(0xFFC0CB),
    BROWN(0x964B00),
    GREY(0x808080),
    TURQUOISE(0x1ABC9C),
    SILVER(0xB1B1B1),
    GOLD(0xF8E231),
    COPPER(0xFF9900),
    LIME(0x32CD32),
    CRIMSON(0xDC143C),
    MAGENTA(0xFF00FF),
    CYAN(0x00FFFF),
    TEAL(0x0097A7),
    CORAL(0xFFC67D),
    SALMON(0xFA8072),
    VIOLET(0x800080),
    CHARCOAL(0x333333),
    MINT(0xB2FFFC),
    LAVENDER(0xC7B8EA),
    PEACH(0xFFD7BE),
    NAVY(0x03055B),
    MAROON(0x800000);

    fun toLunarClientColor(): Color = Color.newBuilder()
        .setColor(this.color)
        .build()

}