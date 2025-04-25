package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.dto.AddPinnedServerDTO
import org.cubewhy.celestial.entity.dto.PatchPinnedServerDTO
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.springframework.security.core.Authentication

interface PinnedServerService {
    suspend fun listOwnedServers(authentication: Authentication): List<PinnedServerVO>
    suspend fun addPinnedServer(authentication: Authentication, dto: AddPinnedServerDTO): PinnedServerVO
    suspend fun patchPinnedServer(authentication: Authentication, pinnedServerId: String, dto: PatchPinnedServerDTO): PinnedServerVO
    suspend fun deletePinnedServer(authentication: Authentication, pinnedServerId: String)
}