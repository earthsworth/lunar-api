package org.cubewhy.celestial.entity

@Suppress("UNUSED")
enum class Role(
    vararg val availableLogoColors: LogoColor,
) {
    USER(LogoColor.WHITE),
    ADMIN(*LogoColor.entries.toTypedArray()),
    STAFF(LogoColor.GREEN),
    DEVELOPER(LogoColor.CYAN),
    YELLOW_FISH(LogoColor.YELLOW),
    SPONSOR(LogoColor.ORANGE),
    PARTNER(LogoColor.GOLD),

    LUNAR_CN_2K25(LogoColor.BLACK),

    MEDIA(LogoColor.SKY),
    MEDIA_PLUS(LogoColor.LIGHT_PURPLE),

    HELPER(LogoColor.SAGE_GREEN),
    HELPER_PLUS(LogoColor.LIME),

    TESTER(LogoColor.DEEP_CYAN),
}