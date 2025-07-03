package org.cubewhy.celestial.controller.lunar

import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.StyngrJwtVO
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.util.extractBaseUrl
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/lunar/styngr")
class LunarStyngrController(
    private val jamService: JamService,
) {

    @GetMapping("jams")
    suspend fun jams(exchange: ServerWebExchange): List<LunarSongVO> {
        return jamService.availableSongs(exchange.extractBaseUrl())
    }

    @PostMapping("jwt")
    suspend fun jwt(exchange: ServerWebExchange): StyngrJwtVO {
        // the forged api use the same token for styngr
        val jwt = exchange.request.headers.getFirst("Authorization") as String
        return StyngrJwtVO(
            styngrJwt = jwt
        )
    }
}