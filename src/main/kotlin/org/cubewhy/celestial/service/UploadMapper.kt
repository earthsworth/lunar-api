package org.cubewhy.celestial.service

import org.cubewhy.celestial.entity.Upload
import org.cubewhy.celestial.entity.vo.UploadVO

interface UploadMapper {
    fun mapToUploadVO(upload: Upload): UploadVO
}