package org.cubewhy.celestial.controller.lunar

import org.cubewhy.celestial.entity.GameMetadataResponse
import org.cubewhy.celestial.service.GameService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/lunar/game")
class LunarGameController(private val gameService: GameService) {
    @GetMapping("metadata")
    suspend fun metadata(@RequestParam(required = false) branch: String, exchange: ServerWebExchange): GameMetadataResponse {
        return gameService.metadata(branch, exchange)
    }
}