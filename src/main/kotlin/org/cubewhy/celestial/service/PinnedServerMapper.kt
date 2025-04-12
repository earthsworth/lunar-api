package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.PinnedServer
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.cubewhy.celestial.entity.vo.StarServerVO

interface PinnedServerMapper {
    fun mapToPinedServerVO(pinnedServer: PinnedServer): PinnedServerVO
    fun mapToStarServerVO(pinnedServer: PinnedServer): StarServerVO?
}