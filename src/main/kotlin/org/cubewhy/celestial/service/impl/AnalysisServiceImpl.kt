package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Analysis
import org.cubewhy.celestial.repository.AnalysisRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.repository.WebUserRepository
import org.cubewhy.celestial.service.AnalysisService
import org.cubewhy.celestial.service.SessionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class AnalysisServiceImpl(
    private val scope: CoroutineScope,
    private val analysisRepository: AnalysisRepository,
    private val userRepository: UserRepository,
    private val webUserRepository: WebUserRepository,
    private val sessionService: SessionService
) : AnalysisService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun getNowAnalysis(): Analysis {
        return Analysis(
            userCount = userRepository.count().awaitFirst(),
            webUserCount = webUserRepository.count().awaitFirst(),
            onlineCount = sessionService.countAvailableSessions()
        )
    }

    @Scheduled(cron = "0 0 * * * *")
    private fun record() {
        scope.launch {
            val onlineCount = sessionService.countAvailableSessions()
            val userCount = userRepository.count().awaitFirst()
            val webUserCount = webUserRepository.count().awaitFirst()
            logger.debug { "Record analysis data (total $userCount users, $webUserCount web users, $onlineCount online)" }
            val analysis = Analysis(
                userCount = userCount,
                webUserCount = webUserCount,
                onlineCount = onlineCount
            )
            // save analysis
            analysisRepository.save(analysis).awaitFirst()
        }
    }

    override suspend fun getLatestAnalysis(): Analysis =
        analysisRepository.findFirstByOrderByTimestampDesc().awaitFirst()
}