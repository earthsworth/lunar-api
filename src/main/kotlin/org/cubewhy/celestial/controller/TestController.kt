package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
data class TestController(
    val userService: UserService
) {
    @GetMapping("/test")
    suspend fun test(): User {
        return userService.loadUser("test", "0")
    }
}