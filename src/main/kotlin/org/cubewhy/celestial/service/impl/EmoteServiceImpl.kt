package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.emote.v1.WebsocketEmoteV1
import com.opencsv.CSVReader
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.cubewhy.celestial.entity.Cosmetic
import org.cubewhy.celestial.entity.Emote
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.service.EmoteService
import org.cubewhy.celestial.service.impl.CosmeticServiceImpl.Companion
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.io.InputStreamReader

@Service
class EmoteServiceImpl : EmoteService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val emoteList = mutableListOf<Emote>()

    @PostConstruct
    private fun init(){
        logger.info { "Loading emotes from csv" }
        val resource = ClassPathResource("emote/emotes.csv")
        // load cosmetics
        resource.inputStream.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                CSVReader(reader).use { csvReader ->
                    csvReader.forEach { row ->
                        if (row.size >= 4) { // 确保至少有 4 个字段
                            val id = row[0].trim().toInt()
                            val name = row[1].trim()
                            emoteList.add(Emote(id, name))
                        }
                    }
                }
            }
        }
        logger.info { "Loaded ${emoteList.size} emotes" }
    }

    override suspend fun processLogin(user: User) : GeneratedMessage {
        return WebsocketEmoteV1.LoginResponse.newBuilder().apply {
            addAllOwnedEmotes(emoteList.map { it.toOwnedEmote(it.emoteId) })
            addAllOwnedEmoteIds(emoteList.map {it.toId(it.emoteId)})
            addAllEquippedEmoteIds(emoteList.map { it.toId(it.emoteId) })
            hasAllEmotesFlag = true
            lunarPlusFreeEmoteId = 7
        }.build()
    }


    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        when(method) {
            "Login" -> {
                return processLogin(user)
            }
            else -> return null
        }
    }
}