package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.entity.vo.AnalysisVO
import org.cubewhy.celestial.service.AnalysisService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = ["*"])
class AnalysisController(
    private val analysisService: AnalysisService
) {
    /**
     * @param after timestamp, after this time's analysis
     */
    @GetMapping
    suspend fun analysis(@RequestParam after: Long): RestBean<List<AnalysisVO>> {
        return RestBean.success(analysisService.getAnalysisAfter(Instant.ofEpochSecond(after)))
    }

    @GetMapping("/now")
    suspend fun now(): RestBean<Analysis> {
        return RestBean.success(analysisService.getNowAnalysis())
    }
}