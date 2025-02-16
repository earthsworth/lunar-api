package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.PlusColor
import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.dto.EditRoleDTO
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

    override suspend fun togglePlus(playerName: String): RestBean<Void> {
        val user = userRepository.findByUsernameIgnoreCase(playerName).awaitFirst()
        val newState = !user.cosmetic.lunarPlusState
        logger.info { "User ${user.username} ${if (newState) "enabled" else "disabled"} Lunar+ feature" }
        user.cosmetic.lunarPlusColor = if (newState) PlusColor.AQUA.color else 0
        userRepository.save(user).awaitFirst()
        return RestBean.success()
    }

    override suspend fun editRole(dto: EditRoleDTO) {
        val target = userRepository.findByUsernameIgnoreCase(dto.user).awaitFirst()
        target.role = Role.valueOf(dto.role)
        userRepository.save(target).awaitFirst()
    }

    override suspend fun playerInfo(playerName: String): RestBean<PlayerInfoVO> {
        val target = userRepository.findByUsernameIgnoreCase(playerName).awaitFirst()
        val res = PlayerInfoVO(
            user = playerName,
            online = sessionService.getSession(target) != null,
            mcName = target.username,
            mcUuid = target.uuid,
            roleColor = target.role.color,
            roleRank = target.role.rank,
            plus = target.cosmetic.lunarPlusState,
        )
        return RestBean.success(res)
    }
}