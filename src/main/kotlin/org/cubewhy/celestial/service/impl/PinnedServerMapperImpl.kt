package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.PinnedServer
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.cubewhy.celestial.entity.vo.StarServerVO
import org.cubewhy.celestial.service.PinnedServerMapper
import org.springframework.stereotype.Service

@Service
class PinnedServerMapperImpl : PinnedServerMapper {
    override fun mapToPinedServerVO(pinnedServer: PinnedServer): PinnedServerVO {
        return PinnedServerVO(
            id = pinnedServer.id!!,
            name = pinnedServer.name,
            ip = pinnedServer.address,
            versions = pinnedServer.minecraftVersions,
            removable = pinnedServer.removable
        )
    }

    override fun mapToStarServerVO(pinnedServer: PinnedServer): StarServerVO? {
        val regex = pinnedServer.starRegex ?: return null
        return StarServerVO(regex)
    }
}