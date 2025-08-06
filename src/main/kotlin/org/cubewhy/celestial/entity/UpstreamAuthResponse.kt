package org.cubewhy.celestial.entity

import com.lunarclient.authenticator.v1.AuthSuccessMessage
import com.lunarclient.authenticator.v1.ClientboundWebSocketMessage
import com.lunarclient.authenticator.v1.EncryptionRequestMessage

data class UpstreamAuthResponse(
    val encryptRequest: EncryptionRequestMessage?,
    val authSuccessMessage: AuthSuccessMessage?,
) {

    companion object {
        fun from(response: ClientboundWebSocketMessage): UpstreamAuthResponse {
            return UpstreamAuthResponse(
                if (response.hasEncryptionRequest()) response.encryptionRequest else null,
                if (response.hasAuthSuccess()) response.authSuccess else null
            )
        }
    }


}
