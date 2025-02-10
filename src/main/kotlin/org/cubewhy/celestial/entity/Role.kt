package org.cubewhy.celestial.entity

enum class Role(val color: Int, val rank: String) {
    USER(0xFFFFFF, "User"),
    STAFF(0x2EE101, "Staff"),
    ADMIN(0xE10110, "Admin"),
    DEVELOPER(0x00FFF0, "Dev"),
    OWNER(0x96010E, "Owner")
}