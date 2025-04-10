package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.vo.PlayerInfoVO
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.AdminService
import org.cubewhy.celestial.service.SessionService
import org.springframework.stereotype.Service

@Service
class AdminServiceImpl(
    private val userRepository: UserRepository,
    private val sessionService: SessionService
) : AdminService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun playerInfo(playerName: String): PlayerInfoVO {
        val target = userRepository.findByUsernameIgnoreCase(playerName).awaitFirst()
        val res = PlayerInfoVO(
            user = playerName,
            online = sessionService.isOnline(target),
            mcName = target.username,
            mcUuid = target.uuid,
            roleColor = target.cosmetic.lunarLogoColor.color,
            roles = target.resolvedRoles,
            plus = target.cosmetic.lunarPlusState,
        )
        return res
    }
}