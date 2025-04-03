package org.cubewhy.celestial.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class SavedCosmetics(
    @Id var id: String? = null,

    val cosmetics: UserCosmeticSettings
)
