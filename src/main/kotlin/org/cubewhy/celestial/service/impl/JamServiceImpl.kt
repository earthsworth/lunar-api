package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.jam.v1.LoginResponse
import com.lunarclient.websocket.jam.v1.OwnedJam
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.util.toProtobufType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Instant

@Service
class JamServiceImpl : JamService {
    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        when (method) {
            "Login" -> LoginResponse.newBuilder().apply {
                this.ownedJamsList.add( OwnedJam.newBuilder().apply {
                    this.jamId = 0
                    this.grantedAt = Instant.now().toProtobufType()
                }.build())
            }.build().toWebsocketResponse()
        }
        return emptyWebsocketResponse()
    }
}