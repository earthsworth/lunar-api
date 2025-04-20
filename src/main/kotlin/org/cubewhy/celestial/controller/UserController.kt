package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.UpdatePasswordDTO
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @GetMapping
    suspend fun selfInfo(@AuthenticationPrincipal authentication: Authentication): ResponseEntity<RestBean<UserVO>> {
        return ResponseEntity.ok(RestBean.success(userService.selfInfo(authentication)))
    }

    @PostMapping("password")
    suspend fun updatePasword(
        @AuthenticationPrincipal authentication: Authentication,
        dto: UpdatePasswordDTO
    ): ResponseEntity<RestBean<*>> {
        return ResponseEntity.ok(RestBean.success(userService.updatePassword(authentication, dto)))
    }
}