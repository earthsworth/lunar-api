package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.socials.v1.LoginRequest
import com.lunarclient.websocket.socials.v1.LoginResponse
import com.lunarclient.websocket.socials.v1.SocialsVisibility
import org.cubewhy.celestial.entity.RpcResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.service.SocialsService
import org.springframework.stereotype.Service

@Service
class SocialsServiceImpl : SocialsService {
    override val serviceName: String = "lunarclient.websocket.socials.v1.SocialsService"

    override suspend fun process(
        method: String,
        payload: ByteString,
        connection: ClientConnection<*>,
        user: User
    ): RpcResponse {
        return when (method) {
            "Login" -> processLogin(LoginRequest.parseFrom(payload), connection, user).toWebsocketResponse()
            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processLogin(
        payload: LoginRequest,
        connection: ClientConnection<*>,
        user: User,
    ): LoginResponse {
        return LoginResponse.newBuilder().apply {
            this.socialsVisibility = SocialsVisibility.SOCIALS_VISIBILITY_NOONE
        }.build()
    }
}