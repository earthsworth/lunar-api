package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.GameMetadataResponse
import org.cubewhy.celestial.service.GameService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/game")
class GameController(private val gameService: GameService) {
    @GetMapping("metadata")
    suspend fun metadata(@RequestParam branch: String): GameMetadataResponse {
        return gameService.metadata(branch)
    }
}