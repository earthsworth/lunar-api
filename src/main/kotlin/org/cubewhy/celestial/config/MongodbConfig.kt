package org.cubewhy.celestial.config

import org.cubewhy.celestial.converer.RoleToStringConverter
import org.cubewhy.celestial.converer.StringToRoleConverter
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