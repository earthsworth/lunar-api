package org.cubewhy.celestial.converer

import org.cubewhy.celestial.entity.Role
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class RoleToStringConverter : Converter<Role, String> {
    override fun convert(source: Role): String? {
        if (source == Role.USER) return null
        return source.name
    }
}