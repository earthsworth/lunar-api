package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.RegisterUserDTO
import org.cubewhy.celestial.entity.dto.ResetPasswordDTO
import org.cubewhy.celestial.entity.vo.UserVO
import org.cubewhy.celestial.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @PostMapping("register")
    suspend fun register(@RequestBody dto: RegisterUserDTO): RestBean<UserVO> {
        val vo = userService.registerWebUser(dto) ?: return RestBean.failure(409, "User exists")
        return RestBean.success(vo)
    }

    @PostMapping("password")
    suspend fun resetPassword(
        @RequestBody dto: ResetPasswordDTO,
        @AuthenticationPrincipal authentication: Authentication
    ): RestBean<Nothing> {
        try {
            userService.resetWebUserPassword(dto, authentication)
        } catch (e: IllegalArgumentException) {
            return RestBean.failure(400, e.message!!)
        }
        return RestBean.success()
    }
}