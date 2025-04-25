package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.dto.AddPinnedServerDTO
import org.cubewhy.celestial.entity.dto.PatchPinnedServerDTO
import org.cubewhy.celestial.entity.vo.PinnedServerVO
import org.cubewhy.celestial.service.PinnedServerService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pinned-server")
class PinnedServerController(
    private val pinnedServerService: PinnedServerService
) {
    @GetMapping
    suspend fun listOwnedServers(@AuthenticationPrincipal authentication: Authentication): ResponseEntity<RestBean<List<PinnedServerVO>>> {
        return ResponseEntity.ok(RestBean.success(pinnedServerService.listOwnedServers(authentication)))
    }

    @PostMapping
    suspend fun addPinnedServer(
        @AuthenticationPrincipal authentication: Authentication,
        @RequestBody dto: AddPinnedServerDTO
    ): ResponseEntity<RestBean<PinnedServerVO>> {
        return ResponseEntity.ok(RestBean.success(pinnedServerService.addPinnedServer(authentication, dto)))
    }

    @PatchMapping("{serverId}")
    suspend fun patchPinnedServer(
        @AuthenticationPrincipal authentication: Authentication,
        @PathVariable("serverId") serverId: String,
        @RequestBody dto: PatchPinnedServerDTO
    ): ResponseEntity<RestBean<PinnedServerVO>> {
        return ResponseEntity.ok(RestBean.success(pinnedServerService.patchPinnedServer(authentication, serverId, dto)))
    }

    @DeleteMapping("{serverId}")
    suspend fun deletePinnedServer(
        @AuthenticationPrincipal authentication: Authentication,
        @PathVariable("serverId") serverId: String,
    ): ResponseEntity<RestBean<*>> {
        pinnedServerService.deletePinnedServer(authentication, serverId)
        return ResponseEntity.ok(RestBean.success<Nothing>())
    }
}