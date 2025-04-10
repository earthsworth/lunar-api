package org.cubewhy.celestial.config

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.vo.AuthorizeVO
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.JwtUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil,
    private val userService: UserService,
//    private val apiTokenFilter: ApiTokenFilter,
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {

            authorizeExchange {
//                authorize(
//                    pathMatchers("/api/lunar/styngr/jwt"),
//                    authenticated
//                )
                authorize(
                    pathMatchers("/api/user/login", "/api/analysis", "/api/analysis/**", "/api/lunar/**", "/ws", "/ws/**"),
                    permitAll
                )
                authorize(pathMatchers("/api/admin/**"), hasAnyRole(Role.ADMIN.name))
                authorize(anyExchange, authenticated)
            }
            formLogin {
                loginPage = "/api/user/login"
                authenticationSuccessHandler = AuthSuccessHandler(jwtUtil, userService)
                authenticationFailureHandler = AuthFailureHandler
            }
            // todo api token filter
//            addFilterBefore(apiTokenFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            logout {
                logoutUrl = "/api/user/logout"
                logoutSuccessHandler = LogoutSuccessHandler(jwtUtil = jwtUtil)
            }
            exceptionHandling {
                authenticationEntryPoint = UnauthorizedHandler
                accessDeniedHandler = AccessDenyHandler
            }
            csrf {
                disable()
            }
            cors {
                disable()
            }
            httpBasic { }
        }
    }

    class AuthSuccessHandler(private val jwtUtil: JwtUtil, private val userService: UserService) :
        ServerAuthenticationSuccessHandler {
        override fun onAuthenticationSuccess(
            webFilterExchange: WebFilterExchange,
            authentication: Authentication
        ): Mono<Void> {
            // generate JWT
            val details = authentication.principal as User
            // find user
            return userService.loadUserByUsername(details.username).flatMap { user ->
                val jwt = jwtUtil.createJwt(user)
                // parse jwt
                val parsedJwt = jwtUtil.resolveJwt(jwt)!!
                AuthorizeVO(
                    username = user.username,
                    token = jwt,
                    expire = parsedJwt.expiresAt.time,
                    roles = user.resolvedRoles.map { it.name }).toMono()
            }.flatMap { vo ->
                webFilterExchange.exchange.responseSuccess(vo)
            }
        }
    }

    object AuthFailureHandler : ServerAuthenticationFailureHandler {
        override fun onAuthenticationFailure(
            webFilterExchange: WebFilterExchange,
            exception: AuthenticationException
        ): Mono<Void> {
            return webFilterExchange.exchange.responseFailure(401, exception.message!!)
        }
    }

    class LogoutSuccessHandler(private val jwtUtil: JwtUtil) : ServerLogoutSuccessHandler {
        override fun onLogoutSuccess(webFilterExchange: WebFilterExchange, authentication: Authentication): Mono<Void> {
            val token = webFilterExchange.exchange.request.headers[HttpHeaders.AUTHORIZATION]?.get(0)
            if (token.isNullOrEmpty()) {
                return webFilterExchange.exchange.responseFailure(400, "Bad Request")
            }
            // expire token
            return jwtUtil.expireToken(token).flatMap { result ->
                return@flatMap if (result) {
                    webFilterExchange.exchange.responseSuccess(null) // success
                } else {
                    webFilterExchange.exchange.responseFailure(400, "Bad Request") // bad token
                }
            }
        }
    }

    object UnauthorizedHandler : ServerAuthenticationEntryPoint {
        override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
            return exchange.responseFailure(401, ex.message!!)
        }
    }

    object AccessDenyHandler : ServerAccessDeniedHandler {
        override fun handle(exchange: ServerWebExchange, denied: AccessDeniedException): Mono<Void> {
            return exchange.responseFailure(403, denied.message!!)
        }
    }
}

fun <T> ServerWebExchange.responseSuccess(data: T?): Mono<Void> {
    this.response.statusCode = HttpStatus.OK
    this.response.headers.contentType = MediaType.APPLICATION_JSON
    return this.response.writeWith(
        this.response.bufferFactory()
            .wrap(RestBean.success<T?>(data).toJson().encodeToByteArray()).toMono()
    ).then(Mono.defer { this.response.setComplete() })
}

fun ServerWebExchange.responseFailure(code: Int, message: String): Mono<Void> {
    this.response.statusCode = HttpStatus.valueOf(code)
    this.response.headers.contentType = MediaType.APPLICATION_JSON
    return this.response.writeWith(
        this.response.bufferFactory()
            .wrap(RestBean.failure<Nothing?>(code, message).toJson().encodeToByteArray()).toMono()
    ).then(Mono.defer { this.response.setComplete() })
}