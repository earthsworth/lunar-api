package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.language.v1.WebsocketLanguageV1
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.LanguageService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class LanguageServiceImpl(private val userRepository: UserRepository) : LanguageService {
    override fun processUpdateLanguageRequest(request: WebsocketLanguageV1.UpdateLanguageRequest, user: User) {
        user.language = request.newLanguage.minecraftCode
        userRepository.save(user).subscribe()
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        when(method) {
            "UpdateLanguageRequest" ->
                processUpdateLanguageRequest(WebsocketLanguageV1.UpdateLanguageRequest.parseFrom(payload), user)
        }
        return null
    }
}