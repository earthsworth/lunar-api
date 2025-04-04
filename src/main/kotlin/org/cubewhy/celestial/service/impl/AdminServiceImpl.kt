package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.entity.dto.TogglePlusDTO
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

    override suspend fun togglePlus(dto: TogglePlusDTO) {
        val user = userRepository.findByUsernameIgnoreCase(dto.playerName).awaitFirst()
        logger.info { "User ${user.username} ${if (dto.state) "enabled" else "disabled"} Lunar+ feature" }
        user.cosmetic.lunarPlusColor = if (dto.state) PlusColor.AQUA.color else 0
        userRepository.save(user).awaitFirst()
    }

    override suspend fun editRole(dto: EditRoleDTO) {
        val target = userRepository.findByUsernameIgnoreCase(dto.user).awaitFirst()
        target.role = Role.valueOf(dto.role)
        userRepository.save(target).awaitFirst()
    }

    override suspend fun playerInfo(playerName: String): PlayerInfoVO {
        val target = userRepository.findByUsernameIgnoreCase(playerName).awaitFirst()
        val res = PlayerInfoVO(
            user = playerName,
            online = sessionService.isOnline(target),
            mcName = target.username,
            mcUuid = target.uuid,
            roleColor = target.role.color,
            roleRank = target.role.rank,
            plus = target.cosmetic.lunarPlusState,
        )
        return res
    }
}