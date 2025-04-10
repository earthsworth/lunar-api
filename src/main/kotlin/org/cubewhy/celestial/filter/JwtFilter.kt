package org.cubewhy.celestial.filter

import org.cubewhy.celestial.config.responseFailure
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.JwtUtil
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtFilter(
    private val jwtUtil: JwtUtil,
    private val userService: UserService
) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return chain.filter(exchange)
        // parse token
        val jwt =
            jwtUtil.resolveJwt(jwtUtil.convertToken(token)) ?: return exchange.responseFailure(401, "Unauthorized")
        val username = (jwt.claims["name"] ?: return exchange.responseFailure(401, "Bad Token")).asString()
        return userService.findByUsername(username).flatMap { userDetails ->
            val auth: Authentication = UsernamePasswordAuthenticationToken(userDetails, userDetails.password, userDetails.authorities)
            val securityContext: SecurityContext = SecurityContextImpl(auth)
            return@flatMap chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        }
    }
}