package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.vo.UploadVO
import org.springframework.web.server.ServerWebExchange

interface UploadService {
    suspend fun upload(exchange: ServerWebExchange): UploadVO
}