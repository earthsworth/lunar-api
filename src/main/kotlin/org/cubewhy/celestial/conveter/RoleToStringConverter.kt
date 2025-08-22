package org.cubewhy.celestial.conveter

import org.cubewhy.celestial.entity.Role
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class RoleToStringConverter : Converter<Role, String> {
    override fun convert(source: Role): String {
        return source.name
    }
}