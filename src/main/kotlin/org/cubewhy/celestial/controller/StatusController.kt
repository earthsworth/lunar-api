package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.vo.StatusVO
import org.cubewhy.celestial.service.StatusService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/status")
class StatusController(private val statusService: StatusService) {
    @GetMapping("/")
    suspend fun status(): RestBean<StatusVO> {
        return RestBean.success(statusService.getStatus())
    }
}