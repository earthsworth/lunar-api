package org.cubewhy.celestial.util

import java.util.regex.Pattern

object MinecraftServerAddressValidator {
    fun isValidServerAddress(address: String): Boolean {
        if (address.isBlank()) return false

        return when {
            address.contains(":") && address.count { it == ':' } > 1 -> {
                if (address.startsWith("[") && address.contains("]:")) {
                    val ipv6End = address.indexOf("]:")
                    val host = address.substring(1, ipv6End)
                    val port = address.substring(ipv6End + 2)
                    isValidIpv6(host) && isValidPort(port)
                } else if (address.count { it == ':' } == 1) {
                    val parts = address.split(":")
                    isValidHost(parts[0]) && isValidPort(parts[1])
                } else {
                    false
                }
            }

            address.startsWith("[") && address.endsWith("]") -> {
                isValidIpv6(address.substring(1, address.length - 1))
            }

            isPotentialIpv6(address) -> {
                isValidIpv6(address)
            }

            else -> isValidHost(address)
        }
    }

    private fun isValidHost(host: String): Boolean {
        return isValidIpv4(host) || isValidIpv6(host) || isValidDomain(host)
    }

    private fun isValidIpv4(ip: String): Boolean {
        val ipv4Pattern = Pattern.compile(
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        return ipv4Pattern.matcher(ip).matches()
    }

    private fun isValidIpv6(ip: String): Boolean {
        // 简化的IPv6正则表达式，覆盖大多数常见格式
        val ipv6Pattern = Pattern.compile(
            "^(" +
                "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|" +

                "(([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})?::(([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})?|" +

                "([0-9a-fA-F]{1,4}:){1,7}:|" +
                ":(:[0-9a-fA-F]{1,4}){1,7}|" +

                "::(ffff|FFFF)(:0{1,4})?:((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)|" +
                "([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)" +
                ")$"
        )

        if (ip.startsWith("[") && ip.endsWith("]")) {
            return ipv6Pattern.matcher(ip.substring(1, ip.length - 1)).matches()
        }

        return ipv6Pattern.matcher(ip).matches()
    }

    private fun isPotentialIpv6(address: String): Boolean {
        return address.count { it == ':' } >= 2 &&
            !address.contains(".") &&
            !address.contains("[") &&
            !address.contains("]")
    }

    private fun isValidDomain(domain: String): Boolean {
        val domainPattern = Pattern.compile(
            "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\." +
                "(?!-)[A-Za-z0-9-]{1,63}(?<!-))*\\.[A-Za-z]{2,6}$"
        )
        return domainPattern.matcher(domain).matches()
    }

    private fun isValidPort(port: String): Boolean {
        return try {
            val portNum = port.toInt()
            portNum in 1..65535
        } catch (_: NumberFormatException) {
            false
        }
    }
}