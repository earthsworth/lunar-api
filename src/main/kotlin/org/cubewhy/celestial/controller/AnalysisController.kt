package org.cubewhy.celestial.controller

import org.cubewhy.celestial.entity.Analysis
import org.cubewhy.celestial.entity.RestBean
import org.cubewhy.celestial.service.AnalysisService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/analysis")
class AnalysisController(
    private val analysisService: AnalysisService
) {
    /**
     * @param after timestamp, after this time's analysis
     */
    @PostMapping("/")
    suspend fun analysis(@RequestParam after: Long): RestBean<List<Analysis>> {
        return RestBean.success(analysisService.getAnalysisAfter(Instant.ofEpochSecond(after)))
    }
}