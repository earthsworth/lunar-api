package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.vo.StatusVO

interface StatusService {
    suspend fun getStatus(): StatusVO
}