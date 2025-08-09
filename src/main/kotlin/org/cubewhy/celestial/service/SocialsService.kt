package org.cubewhy.celestial.service

import com.lunarclient.websocket.socials.v1.LoginRequest
import com.lunarclient.websocket.socials.v1.LoginResponse
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.protocol.ClientConnection

interface SocialsService : PacketProcessor {
    suspend fun processLogin(payload: LoginRequest, connection: ClientConnection<*>, user: User): LoginResponse
}