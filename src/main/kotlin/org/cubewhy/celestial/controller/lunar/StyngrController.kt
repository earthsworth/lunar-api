package org.cubewhy.celestial.controller.lunar

import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.service.JamService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/lunar/styngr")
class StyngrController(
    private val jamService: JamService,
) {

    @GetMapping("jams")
    suspend fun jams(exchange: ServerWebExchange): List<SongVO> {
        return jamService.availableSongs()
    }
}