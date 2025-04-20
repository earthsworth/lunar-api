package org.cubewhy.celestial.controller.admin

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.vo.PlayerInfoVO
import org.cubewhy.celestial.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/users")
class AdminUserController(
    private val userService: UserService
) {
    @GetMapping("{username}/player")
    suspend fun playerInfo(@PathVariable("username") username: String): ResponseEntity<RestBean<PlayerInfoVO>> {
        return ResponseEntity.ok(RestBean.success(userService.getPlayerInfo(username)))
    }

    @GetMapping("{username}/roles")
    suspend fun userRoles(@PathVariable("username") username: String): ResponseEntity<RestBean<List<String>>> {
        return ResponseEntity.ok(RestBean.success(userService.getUserRoles(username)))
    }
}