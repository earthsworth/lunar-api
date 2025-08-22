package org.cubewhy.celestial.entity

@Suppress("UNUSED")
enum class Role(
    vararg val availableLogoColors: LogoColor,
) {
    USER(LogoColor.WHITE, LogoColor.PINK),
    ADMIN(*LogoColor.entries.toTypedArray()),
    STAFF(LogoColor.GREEN, LogoColor.PURPLE),
    DEVELOPER(LogoColor.CYAN),
    YELLOW_FISH(LogoColor.YELLOW),
    SPONSOR(LogoColor.ORANGE),
    PARTNER(LogoColor.GOLD),

    LUNAR_CN_2K25(LogoColor.BLACK),

    MEDIA(LogoColor.SKY, LogoColor.LIGHT_PURPLE),

    HELPER(LogoColor.SAGE_GREEN, LogoColor.LIME),

    TESTER(LogoColor.DEEP_CYAN), ;


    companion object {
        fun fromDb(raw: String?): Role {
            if (raw == null) return USER
            return try {
                Role.valueOf(raw)
            } catch (exp: IllegalArgumentException) {
                // map to USER
                USER
            }
        }
    }
}