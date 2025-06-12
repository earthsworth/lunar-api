package org.cubewhy.celestial.util

import java.security.MessageDigest

fun sha256(input: String): String {
    val bytes = input.toByteArray(Charsets.UTF_8)
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}