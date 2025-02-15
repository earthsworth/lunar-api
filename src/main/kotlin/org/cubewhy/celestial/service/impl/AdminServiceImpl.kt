package org.cubewhy.celestial.service.impl

import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Role
import org.cubewhy.celestial.entity.dto.EditRoleDTO
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.AdminService
import org.springframework.stereotype.Service

@Service
class AdminServiceImpl(private val userRepository: UserRepository) : AdminService {

    override suspend fun editRole(dto: EditRoleDTO) {
        val target = userRepository.findByUsernameIgnoreCase(dto.user).awaitFirst()
        target.role = Role.valueOf(dto.role)
        userRepository.save(target).awaitFirst()
    }
}