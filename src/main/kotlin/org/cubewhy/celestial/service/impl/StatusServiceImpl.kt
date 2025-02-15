package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.vo.StatusVO
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.StatusService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StatusServiceImpl(private val sessionService: SessionService) : StatusService {
    override suspend fun getStatus(): StatusVO {
        return StatusVO(
            onlinePlayer = sessionService.countAvailableSessions(),
            timeStamp = Instant.now().epochSecond
        )
    }
}