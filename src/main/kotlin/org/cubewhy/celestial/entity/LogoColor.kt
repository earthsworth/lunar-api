package org.cubewhy.celestial.entity

enum class LogoColor(
    val color: Int,
) {
    BLACK(0x010101),
    WHITE(0xFFFFFF),
    GREEN(0x00AAAA),
    PINK(0xFF55FF),
    DARK_RED(0xAA0000),
    RED(0xFF5555),
    CYAN(0x55FFFF),
    YELLOW(0xFFFF55),
    ORANGE(0xFFAA00),
    GOLD(0xFFAA00),
    DEEP_CYAN(0xFFFF55),
    SAGE_GREEN(0x00AAAA),
    SKY(0xFFAA00),
    LIME(0x00AAAA),
    BLUE(0x324BBB),
    DARK_GREEN(0x008000),
    PURPLE(0xAA00AA),
    LIGHT_PURPLE(0xFFAA00), ;

//    companion object {
//        fun findIgnoreCase(name: String): LogoColor? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
//    }
}