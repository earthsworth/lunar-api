package org.cubewhy.celestial.handler

import org.cubewhy.celestial.entity.RestBean
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): Mono<ResponseEntity<RestBean<*>>> {
        // TODO better error message if ex.reason is null
        val errorResponse = RestBean.failure<Any>(ex.statusCode.value(), ex.reason ?: ex.typeMessageCode)
        return Mono.just(ResponseEntity.status(ex.statusCode).body(errorResponse))
    }
}