package org.cubewhy.celestial.controller.styngr

import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO
import org.cubewhy.celestial.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/styngr/v2")
class StyngrController(
    private val userService: UserService
) {

    @PostMapping("sdk/tokens/sdkuser")
    suspend fun sdkUser(@AuthenticationPrincipal authentication: Authentication): StyngrUserVO {
        return userService.loadStyngrUser(authentication)
    }
}