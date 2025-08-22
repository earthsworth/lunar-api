package org.cubewhy.celestial.config

import org.cubewhy.celestial.conveter.RoleToStringConverter
import org.cubewhy.celestial.conveter.StringToRoleConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions


@Configuration
class MongodbConfig {
    @Bean
    fun mongoCustomConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf<Any?>(
                StringToRoleConverter(),
                RoleToStringConverter()
            )
        )
    }
}