package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.celestial.entity.Analysis
import org.cubewhy.celestial.entity.vo.AnalysisVO
import org.cubewhy.celestial.repository.AnalysisRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.AnalysisService
import org.cubewhy.celestial.service.SessionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AnalysisServiceImpl(
    private val scope: CoroutineScope,
    private val analysisRepository: AnalysisRepository,
    private val userRepository: UserRepository,
    private val sessionService: SessionService
) : AnalysisService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun getNowAnalysis(): AnalysisVO {
        return AnalysisVO(
            userCount = userRepository.count().awaitFirst(),
            onlineCount = sessionService.countAvailableSessions(),
            timestamp = Instant.now()
        )
    }

    @Scheduled(cron = "0 0 * * * *")
    private fun record() {
        scope.launch {
            val onlineCount = sessionService.countAvailableSessions()
            val userCount = userRepository.count().awaitFirst()
            logger.info { "Record analysis data (total $userCount users, $onlineCount online)" }
            val analysis = Analysis(
                userCount = userCount,
                onlineCount = onlineCount
            )
            // save analysis
            analysisRepository.save(analysis).awaitFirst()
        }
    }

    override suspend fun getAnalysisAfter(timestamp: Instant): List<AnalysisVO> {
        return analysisRepository.getAnalysisByCreatedAtAfter(timestamp)
            .map { it.toVO() }
            .collectList().awaitLast()
    }
}