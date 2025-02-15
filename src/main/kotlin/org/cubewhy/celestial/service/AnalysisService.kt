package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.Analysis
import java.time.Instant

interface AnalysisService {

    suspend fun getAnalysisAfter(timestamp: Instant): List<Analysis>
}