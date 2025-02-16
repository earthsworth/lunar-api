package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.entity.vo.PlayerInfoVO

interface AdminService {
    suspend fun editRole(dto: EditRoleDTO)
    suspend fun playerInfo(playerName: String): RestBean<PlayerInfoVO>
    suspend fun togglePlus(playerName: String): RestBean<Void>
}