package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.UpstreamAuthResponse
import org.cubewhy.celestial.protocol.ClientConnection

interface ExtendService {
    suspend fun openAuthConnection(): ClientConnection<*>
    suspend fun awaitForAuthResponse(connection: ClientConnection<*>): UpstreamAuthResponse?
}