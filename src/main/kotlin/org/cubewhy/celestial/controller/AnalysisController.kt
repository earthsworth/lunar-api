package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.Analysis
import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.service.AnalysisService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analysis")
class AnalysisController(
    private val analysisService: AnalysisService
) {
    @GetMapping
    suspend fun analysis(): RestBean<Analysis> {
        return RestBean.success(analysisService.getLatestAnalysis())
    }

    @GetMapping("/now")
    suspend fun now(): RestBean<Analysis> {
        return RestBean.success(analysisService.getNowAnalysis())
    }
}