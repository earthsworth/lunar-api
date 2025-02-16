package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.language.v1.WebsocketLanguageV1
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.service.LanguageService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class LanguageServiceImpl : LanguageService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun processUpdateLanguageRequest(
        request: WebsocketLanguageV1.UpdateLanguageRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketLanguageV1.UpdateLanguageResponse {
        logger.info { "User ${user.username} selected new language ${request.newLanguage}" }
        session.attributes["language"] = request.newLanguage
        return WebsocketLanguageV1.UpdateLanguageResponse.getDefaultInstance()
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "UpdateLanguageRequest" ->
                this.processUpdateLanguageRequest(
                    WebsocketLanguageV1.UpdateLanguageRequest.parseFrom(payload),
                    session,
                    user
                ).toWebsocketResponse()

            else -> emptyWebsocketResponse()
        }
    }
}