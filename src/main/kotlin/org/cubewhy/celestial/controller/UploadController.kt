package org.cubewhy.celestial.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.withContext
import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.vo.UploadVO
import org.cubewhy.celestial.service.UploadService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/upload")
class UploadController(private val uploadService: UploadService) {
    @PostMapping
    suspend fun upload(exchange: ServerWebExchange): RestBean<UploadVO> {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext RestBean.success(uploadService.upload(exchange))
            } catch (e: IllegalArgumentException) {
                return@withContext RestBean.failure(400, e)
            }
        }
    }
}