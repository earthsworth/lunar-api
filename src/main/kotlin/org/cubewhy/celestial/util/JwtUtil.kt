package org.cubewhy.celestial.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import org.cubewhy.celestial.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Component
class JwtUtil(
    private val stringReactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
) {
    @Value("\${spring.security.jwt.key}")
    private lateinit var key: String

    @Value("\${spring.security.jwt.expire}")
    private var expire = 0

    fun resolveJwt(token: String?): DecodedJWT? {
        if (token == null) {
            return null // incorrect token
        }
        val algorithm: Algorithm = Algorithm.HMAC256(key)
        val jwtVerifier: JWTVerifier = JWT.require(algorithm).build()
        try {
            val jwt: DecodedJWT = jwtVerifier.verify(token)
            val expireAt: Date = jwt.expiresAt
            return if (Date().after(expireAt)) null else jwt
        } catch (error: JWTVerificationException) {
            // User modified this
            return null
        }
    }

    fun createJwt(user: User): String {
        val algorithm: Algorithm = Algorithm.HMAC256(key)
        return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("id", user.id) // internal id
            .withClaim("name", user.username) // minecraft username
            .withClaim("mcuuid", user.uuid) // minecraft uuid
            .withExpiresAt(expireDate) // now + {date}
            .withIssuedAt(Date()) // time now
            .sign(algorithm)
    }

    private val expireDate: Date
        get() {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR, 24 * expire)
            return calendar.time
        }

    private fun convertToken(headerToken: String?): String? {
        if (headerToken == null || !headerToken.startsWith("Bearer ")) {
            return null // incorrect token
        }
        // cut "Bearer "
        return headerToken.substring(7)
    }

    fun isInvalidToken(tokenId: String): Mono<Boolean> {
        return stringReactiveRedisTemplate.hasKey(Const.EXPIRED_TOKEN + tokenId)
    }

    fun expireToken(token: String): Mono<Boolean> {
        val parsedToken = convertToken(token) ?: return false.toMono()
        // parse token
        val jwt = this.resolveJwt(parsedToken) ?: return false.toMono()
        // expire token
        return stringReactiveRedisTemplate.opsForValue().set(Const.EXPIRED_TOKEN + jwt.id, "0").then(Mono.just(true))
    }
}