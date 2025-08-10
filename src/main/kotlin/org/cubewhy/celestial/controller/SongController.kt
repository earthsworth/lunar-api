package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.CreateSongDTO
import org.cubewhy.celestial.entity.dto.ModifySongDTO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.util.extractBaseUrl
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/song")
class SongController(
    private val jamService: JamService
) {
    @GetMapping
    suspend fun listSongs(
        @AuthenticationPrincipal authentication: Authentication,
        exchange: ServerWebExchange
    ): ResponseEntity<RestBean<List<SongVO>>> {
        return ResponseEntity.ok(RestBean.success(jamService.listOwn(authentication, exchange.extractBaseUrl())))
    }

    @PostMapping
    suspend fun createSong(
        @RequestBody dto: CreateSongDTO,
        @AuthenticationPrincipal authentication: Authentication,
        exchange: ServerWebExchange
    ): ResponseEntity<RestBean<SongVO>> {
        return ResponseEntity.ok(
            RestBean.success(
                jamService.createSong(
                    dto,
                    authentication,
                    exchange.extractBaseUrl()
                )
            )
        )
    }

    @PatchMapping
    suspend fun modifySong(
        @RequestBody dto: ModifySongDTO,
        @AuthenticationPrincipal authentication: Authentication,
        exchange: ServerWebExchange,
    ): ResponseEntity<RestBean<SongVO>> {
        return ResponseEntity.ok(
            RestBean.success(
                jamService.modifySong(
                    dto,
                    authentication,
                    exchange.extractBaseUrl()
                )
            )
        )
    }
}