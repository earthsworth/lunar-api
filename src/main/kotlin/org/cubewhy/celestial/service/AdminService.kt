package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.vo.PlayerInfoVO

interface AdminService {
    suspend fun playerInfo(playerName: String): PlayerInfoVO
}