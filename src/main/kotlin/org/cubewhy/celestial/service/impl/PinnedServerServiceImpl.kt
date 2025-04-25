package org.cubewhy.celestial.service.impl

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.PinnedServer
import org.cubewhy.celestial.entity.dto.AddPinnedServerDTO
import org.cubewhy.celestial.entity.dto.PatchPinnedServerDTO
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.cubewhy.celestial.repository.PinnedServerRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.PinnedServerMapper
import org.cubewhy.celestial.service.PinnedServerService
import org.cubewhy.celestial.util.MinecraftServerAddressValidator
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PinnedServerServiceImpl(
    private val pinnedServerRepository: PinnedServerRepository,
    private val userRepository: UserRepository,
    private val pinnedServerMapper: PinnedServerMapper
) : PinnedServerService {
    override suspend fun listOwnedServers(authentication: Authentication): List<PinnedServerVO> {
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        return pinnedServerRepository.findAllByOwner(user.id!!)
            .map { pinnedServerMapper.mapToPinedServerVO(it) }
            .collectList()
            .awaitFirst()
    }

    override suspend fun addPinnedServer(
        authentication: Authentication,
        dto: AddPinnedServerDTO
    ): PinnedServerVO {
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        // check exists
        if (pinnedServerRepository.existsByAddress(dto.address).awaitFirst()) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "This server already exists"
            )
        }
        // verify address
        if (!MinecraftServerAddressValidator.isValidServerAddress(dto.address)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Bad server address"
            )
        }
        // create pinned server
        val pinnedServer = PinnedServer(
            name = dto.name,
            owner = user.id!!,
            address = dto.address,
            minecraftVersions = dto.minecraftVersions,
            removable = false,
            starRegex = createDomainRegex(dto.address)
        )
        return pinnedServerMapper.mapToPinedServerVO(pinnedServerRepository.save(pinnedServer).awaitFirst())
    }

    override suspend fun patchPinnedServer(
        authentication: Authentication,
        pinnedServerId: String,
        dto: PatchPinnedServerDTO
    ): PinnedServerVO {
        // find pinned server
        val pinnedServer = findServer(authentication, pinnedServerId)
        // mut the server
        dto.address?.let { pinnedServer.address = it }
        dto.name?.let { pinnedServer.name = it }
        dto.address?.let { pinnedServer.address = it }
        return pinnedServerMapper.mapToPinedServerVO(pinnedServerRepository.save(pinnedServer).awaitFirst())
    }

    override suspend fun deletePinnedServer(
        authentication: Authentication,
        pinnedServerId: String
    ) {
        // delete the server
        pinnedServerRepository.delete(findServer(authentication, pinnedServerId)).awaitFirstOrNull()
    }

    private suspend fun findServer(authentication: Authentication, serverId: String): PinnedServer {
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        val pinnedServer = pinnedServerRepository.findById(serverId).awaitFirstOrNull()?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "The server with id $serverId not found"
        )
        // check is the user own this server
        if (user.id!! != pinnedServer.owner) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You have no permissions to edit this server"
            )
        }
        return pinnedServer
    }

    private fun createDomainRegex(domain: String): String {
        val escapedDomain = Regex.escape(domain)
        val regexPattern = "^$escapedDomain$"
        return regexPattern
    }
}