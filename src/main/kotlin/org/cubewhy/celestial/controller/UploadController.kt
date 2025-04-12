package org.cubewhy.celestial.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.vo.UploadVO
import org.cubewhy.celestial.service.UploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/upload")
class UploadController(private val uploadService: UploadService) {
    @PostMapping
    suspend fun upload(exchange: ServerWebExchange): ResponseEntity<RestBean<UploadVO>> {
        return try {
            ResponseEntity.ok(RestBean.success(uploadService.upload(exchange)))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(RestBean.failure(400, e))
        }
    }

    @GetMapping
    suspend fun download(@RequestParam(name = "id") uploadId: String, exchange: ServerWebExchange): ResponseEntity<*>? {
        return withContext(Dispatchers.IO) {
            try {
                uploadService.download(uploadId, exchange)
                return@withContext null
            } catch (e: IllegalArgumentException) {
                return@withContext ResponseEntity.status(404).body(RestBean.failure<Any>(404, "File not found"))
            }
        }
    }
}