package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.language.v1.UpdateLanguageRequest
import com.lunarclient.websocket.language.v1.UpdateLanguageResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.service.LanguageService
import org.springframework.stereotype.Service

@Service
class LanguageServiceImpl : LanguageService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val serviceName: String = "lunarclient.websocket.language.v1.LanguageService"

    override suspend fun processUpdateLanguageRequest(
        request: UpdateLanguageRequest,
        connection: ClientConnection<*>,
        user: User
    ): UpdateLanguageResponse {
        logger.debug { "User ${user.username} selected new language ${request.newLanguage}" }
        connection.metadata.language = request.newLanguage.iso6393Code
        return UpdateLanguageResponse.getDefaultInstance()
    }


    override suspend fun process(
        method: String,
        payload: ByteString,
        connection: ClientConnection<*>,
        user: User
    ): RpcResponse {
        return when (method) {
            "UpdateLanguageRequest" ->
                this.processUpdateLanguageRequest(
                    UpdateLanguageRequest.parseFrom(payload),
                    connection,
                    user
                ).toWebsocketResponse()

            else -> emptyWebsocketResponse()
        }
    }
}