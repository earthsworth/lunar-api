package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.entity.dto.TogglePlusDTO
import org.cubewhy.celestial.entity.vo.PlayerInfoVO

interface AdminService {
    suspend fun editRole(dto: EditRoleDTO)
    suspend fun playerInfo(playerName: String): PlayerInfoVO
    suspend fun togglePlus(dto: TogglePlusDTO)
}