package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.vo.AnalysisVO
import java.time.Instant

interface AnalysisService {
    suspend fun getAnalysisAfter(timestamp: Instant): List<AnalysisVO>

    suspend fun getNowAnalysis(): AnalysisVO
}