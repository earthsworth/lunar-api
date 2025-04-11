package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.CreateSongDTO
import org.cubewhy.celestial.entity.dto.ModifySongDTO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.service.JamService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/song")
class SongController(
    private val jamService: JamService
) {
    @PostMapping
    suspend fun createSong(@RequestBody dto: CreateSongDTO, @AuthenticationPrincipal authentication: Authentication): ResponseEntity<RestBean<SongVO>> {
        return ResponseEntity.ok(RestBean.success(jamService.createSong(dto, authentication)))
    }

//    @PatchMapping
//    suspend fun modifySong(@RequestBody dto: ModifySongDTO, @AuthenticationPrincipal authentication: Authentication): ResponseEntity<RestBean<SongVO>> {
//        return ResponseEntity.ok(RestBean.success(jamService.modifySong(dto, authentication)))
//    }
}