package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.service.AdminService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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


}