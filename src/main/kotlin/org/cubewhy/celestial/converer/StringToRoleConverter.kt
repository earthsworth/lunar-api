package org.cubewhy.celestial.converer

import org.cubewhy.celestial.entity.Role
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

@ReadingConverter
class StringToRoleConverter : Converter<String, Role> {
    override fun convert(source: String): Role {
        return Role.fromDb(source)
    }
}