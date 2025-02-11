package org.cubewhy.celestial.entity

import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1
import com.lunarclient.websocket.cosmetic.v1.WebsocketCosmeticV1.OwnedCosmetic_ExpirationReason
import org.cubewhy.celestial.util.toProtobufType
import java.time.Instant

data class Cosmetic(
    val cosmeticId: Int,
    val name: String,
) {
    fun toUserCosmetic() = UserCosmetic(
        cosmeticId = cosmeticId,
        grantedAt = Instant.now(),
        expiresAt = null,
        expirationReason = null,
    )
}

data class UserCosmetic(
    val cosmeticId: Int,
    val grantedAt: Instant,
    val expiresAt: Instant?,
    val expirationReason: CosmeticExpirationReason?
) {
    fun toEquippedCosmetic(): WebsocketCosmeticV1.EquippedCosmetic = WebsocketCosmeticV1.EquippedCosmetic.newBuilder()
        .setCosmeticId(this.cosmeticId)
        .build()

    fun toOwnedCosmetic(): WebsocketCosmeticV1.OwnedCosmetic {
        return WebsocketCosmeticV1.OwnedCosmetic.newBuilder().apply {
            cosmeticId = this@UserCosmetic.cosmeticId
            grantedAt = this@UserCosmetic.grantedAt.toProtobufType()
            this@UserCosmetic.expirationReason?.let { setExpirationReason(it.toLunarClientType()) }
            this@UserCosmetic.expiresAt?.let { setExpiresAt(it.toProtobufType()) }
        }.build()

    }
}

enum class CosmeticExpirationReason {
    UNSPECIFIED,
    TIME_LAPSED,
    LEFT_LC_DISCORD;

    fun toLunarClientType() = when (this) {
        UNSPECIFIED -> OwnedCosmetic_ExpirationReason.OWNEDCOSMETIC_EXPIRATIONREASON_EXPIRATION_REASON_UNSPECIFIED
        TIME_LAPSED -> OwnedCosmetic_ExpirationReason.OWNEDCOSMETIC_EXPIRATIONREASON_EXPIRATION_REASON_TIME_LAPSED
        LEFT_LC_DISCORD -> OwnedCosmetic_ExpirationReason.OWNEDCOSMETIC_EXPIRATIONREASON_EXPIRATION_REASON_LEFT_LC_DISCORD
    }

}