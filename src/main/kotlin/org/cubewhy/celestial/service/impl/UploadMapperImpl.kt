package org.cubewhy.celestial.service.impl

import org.cubewhy.celestial.entity.Upload
import org.cubewhy.celestial.entity.vo.UploadVO
import org.cubewhy.celestial.service.UploadMapper
import org.springframework.stereotype.Service

@Service
class UploadMapperImpl : UploadMapper {
    override fun mapToUploadVO(upload: Upload) = UploadVO(
        id = upload.id!!,
        sha256 = upload.sha256
    )
}