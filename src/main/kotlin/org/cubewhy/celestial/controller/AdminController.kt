package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.entity.vo.PlayerInfoVO
import org.cubewhy.celestial.service.AdminService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {
    @PostMapping("/editRole")
    suspend fun editRole(
        @RequestBody dto: EditRoleDTO
    ): RestBean<Void> {
        adminService.editRole(dto)
        return RestBean.success()
    }

    @GetMapping("/playerInfo/{playerName}")
    suspend fun getPlayerInfo(@PathVariable playerName: String): RestBean<PlayerInfoVO> {
        return adminService.playerInfo(playerName)
    }

    @GetMapping("/togglePlus/{playerName}")
    suspend fun togglePlus(@PathVariable playerName: String): RestBean<Void> {
        return adminService.togglePlus(playerName)
    }
}