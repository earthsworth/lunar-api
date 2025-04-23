package org.cubewhy.celestial.util

import org.springframework.web.server.ServerWebExchange
import java.net.URI

fun extractBaseUrl(uri: URI): String {
    val url = uri.toURL()
    val protocol = url.protocol
    val host = url.host
    val port = url.port

    return if (port == -1) {
        "$protocol://$host/"
    } else {
        "$protocol://$host:$port/"
    }
}

fun ServerWebExchange.extractBaseUrl() = extractBaseUrl(this.request.uri)

fun parseSizeString(sizeStr: String): Long {
    val pattern = Regex("""(\d+(?:\.\d+)?)(\s*)([a-zA-Z]+)""")
    val units = mapOf(
        "B" to 1L,
        "KB" to 1024L,
        "MB" to 1024L * 1024,
        "GB" to 1024L * 1024 * 1024,
        "TB" to 1024L * 1024 * 1024 * 1024
    )

    var total = 0L

    for (match in pattern.findAll(sizeStr.trim())) {
        val (valueStr, _, unitRaw) = match.destructured
        val value = valueStr.toDouble()
        val unit = unitRaw.uppercase()

        val multiplier = units[unit] ?: throw IllegalArgumentException("Unknown unit: $unit")
        total += (value * multiplier).toLong()
    }

    return total
}

inline fun <reified T : Enum<T>> findEnumByNameIgnoreCase(name: String): T? {
    return enumValues<T>().firstOrNull { it.name.equals(name, ignoreCase = true) }
}