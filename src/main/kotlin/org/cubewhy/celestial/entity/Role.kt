package org.cubewhy.celestial.entity

@Suppress("UNUSED")
enum class Role(vararg availableLogoColors: LogoColor) {
    USER(LogoColor.WHITE),
    ADMIN(*LogoColor.entries.toTypedArray()),
    STAFF(LogoColor.GREEN),
    DEVELOPER(LogoColor.CYAN),
    YELLOW_FISH(LogoColor.YELLOW),
    SPONSOR(LogoColor.ORANGE),
    PARTNER(LogoColor.GOLD),

    LUNAR_CN_2K25(LogoColor.BLACK)
}