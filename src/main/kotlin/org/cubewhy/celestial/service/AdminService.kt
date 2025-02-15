package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.dto.EditRoleDTO

interface AdminService {
    suspend fun editRole(dto: EditRoleDTO)
}