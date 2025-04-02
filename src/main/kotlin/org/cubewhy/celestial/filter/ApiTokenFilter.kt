package org.cubewhy.celestial.filter

import org.cubewhy.celestial.config.responseFailure
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


@Component
class ApiTokenFilter(
    @Value("\${lunar.api.token}") private val apiToken: String
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val providedToken = exchange.request.headers.getFirst("X-API-TOKEN")
        if (providedToken != null) {
            // verify token
            if (providedToken != apiToken) {
                return exchange.responseFailure(401, "Bad Token")
            }
            val auth: Authentication = UsernamePasswordAuthenticationToken(
                "admin", null, mutableListOf<SimpleGrantedAuthority?>(SimpleGrantedAuthority("ROLE_ADMIN"))
            )
            val securityContext: SecurityContext = SecurityContextImpl(auth)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        }
        return chain.filter(exchange)
    }
}