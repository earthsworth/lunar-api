package org.cubewhy.celestial.entity

enum class LogoColor(
    val color: Int,
) {
    BLACK(0x0),
    WHITE(0xFFFFFF),
    GREEN(0x3BBE54),
    PINK(0xFF00EB),
    RED(0xE10110),
    CYAN(0x00FFF0),
    YELLOW(0xEEC42A),
    ORANGE(0xE17E2E),
    GOLD(0xFCAC04),
    DEEP_CYAN(0x07688b),
    SAGE_GREEN(0x749472),
    SKY(0x4cadd0),
    LIME(0x369876),
    LIGHT_PURPLE(0x8579f1), ;

    companion object {
        fun findIgnoreCase(name: String): LogoColor? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}