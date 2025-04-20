package org.cubewhy.celestial.controller.admin

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.vo.PlayerInfoVO
import org.cubewhy.celestial.service.AdminService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("playerInfo/{playerName}")
    suspend fun getPlayerInfo(@PathVariable playerName: String): RestBean<PlayerInfoVO> {
        return RestBean.Companion.success(adminService.playerInfo(playerName))
    }
}