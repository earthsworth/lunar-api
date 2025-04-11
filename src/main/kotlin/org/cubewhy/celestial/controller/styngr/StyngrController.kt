package org.cubewhy.celestial.controller.styngr

import org.cubewhy.celestial.entity.vo.styngr.StyngrSongVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrUserVO
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.service.UserService
import org.cubewhy.celestial.util.extractBaseUrl
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/styngr")
class StyngrController(
    private val userService: UserService,
    private val jamService: JamService
) {

    @PostMapping("/v2/sdk/tokens/sdkuser")
    suspend fun sdkUser(
        @AuthenticationPrincipal authentication: Authentication,
        exchange: ServerWebExchange
    ): StyngrUserVO {
        return userService.loadStyngrUser(authentication, exchange)
    }

    @PostMapping("/v1/sdk/styngs/{songId}/play")
    suspend fun playSong(@PathVariable(name = "songId") songId: String, exchange: ServerWebExchange): StyngrSongVO {
        return jamService.styngrPlaySong(songId, exchange.extractBaseUrl())
    }
}