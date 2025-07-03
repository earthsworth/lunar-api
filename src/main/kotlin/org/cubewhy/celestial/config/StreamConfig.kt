package org.cubewhy.celestial.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.cubewhy.celestial.avro.FederationMessage
import org.cubewhy.celestial.service.SessionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class StreamConfig(
    private val scope: CoroutineScope,
) {

    @Bean
    fun lunarWebsocketPayloadConsumer(sessionService: SessionService): Consumer<FederationMessage> {
        return Consumer { message ->
            scope.launch {
                sessionService.processWithSessionLocally(message.userId) { connection ->
                    // push
                    connection.send(message.payload.array())
                }
            }
        }
    }
}