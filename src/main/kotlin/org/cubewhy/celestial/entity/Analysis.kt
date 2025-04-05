package org.cubewhy.celestial.entity

import org.cubewhy.celestial.entity.vo.AnalysisVO
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.time.Instant

@Document
data class Analysis(
    @Id
    val id: String? = null,

    var userCount: Long,
    var onlineCount: Long,
    val timestamp: Instant = Instant.now()
): Serializable {
    fun toVO() = AnalysisVO(this.userCount, this.onlineCount, timestamp)
}
