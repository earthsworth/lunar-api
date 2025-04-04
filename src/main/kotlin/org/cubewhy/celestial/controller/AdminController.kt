package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.entity.dto.TogglePlusDTO
import org.cubewhy.celestial.entity.vo.PlayerInfoVO
import org.cubewhy.celestial.service.AdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("playerInfo/{playerName}")
    suspend fun getPlayerInfo(@PathVariable playerName: String): RestBean<PlayerInfoVO> {
        return RestBean.success(adminService.playerInfo(playerName))
    }
}