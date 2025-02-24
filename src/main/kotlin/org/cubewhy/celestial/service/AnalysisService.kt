package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.Analysis

interface AnalysisService {

    suspend fun getLatestAnalysis(): Analysis
}