package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.emote.v1.WebsocketEmoteV1
import com.opencsv.CSVReader
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirst
import org.cubewhy.celestial.entity.Emote
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.EmoteService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.pushEvent
import org.cubewhy.celestial.util.toLunarClientUUID
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.io.InputStreamReader

@Service
data class EmoteServiceImpl(
    private val sessionService: SessionService,
    private val subscriptionService: SubscriptionService,
    private val userRepository: UserRepository
) : EmoteService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val emoteList = mutableListOf<Emote>()

    @PostConstruct
    private fun init() {
        logger.info { "Loading emotes from csv" }
        val resource = ClassPathResource("emote/emotes.csv")
        // load emotes
        resource.inputStream.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                CSVReader(reader).use { csvReader ->
                    csvReader.forEach { row ->
                        if (row.size >= 4) {
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

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): GeneratedMessage? {
        return when (method) {
            "Login" -> this.processLogin(user)
            "UseEmote" -> this.processUseEmote(
                WebsocketEmoteV1.UseEmoteRequest.parseFrom(payload),
                session,
                user
            )
            "StopEmote" -> this.processStopEmote(
                session,
                user
            )
            "UpdateEquippedEmotes" -> this.processUpdateEquippedEmotes(
                WebsocketEmoteV1.UpdateEquippedEmotesRequest.parseFrom(payload),
                session,
                user
            )
            else -> null
        }
    }

    override suspend fun processUpdateEquippedEmotes(
        request: WebsocketEmoteV1.UpdateEquippedEmotesRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketEmoteV1.UpdateEquippedEmotesResponse {
        user.emote.equippedEmotes = emoteList.stream().filter {
            request.equippedEmoteIdsList.contains(it.emoteId)
        }.toList()
        userRepository.save(user).awaitFirst()
        return WebsocketEmoteV1.UpdateEquippedEmotesResponse.newBuilder().build()
    }

    override suspend fun processLogin(user: User): GeneratedMessage {
        return WebsocketEmoteV1.LoginResponse.newBuilder().apply {
            addAllOwnedEmotes(emoteList.map { it.toOwnedEmote(it.emoteId) })
            addAllOwnedEmoteIds(emoteList.map { it.emoteId })
            addAllEquippedEmoteIds(user.emote.equippedEmotes.map { it.emoteId })
            hasAllEmotesFlag = true
        }.build()
    }

    override suspend fun processUseEmote(
        request: WebsocketEmoteV1.UseEmoteRequest,
        session: WebSocketSession,
        user: User
    ): WebsocketEmoteV1.UseEmoteResponse {
        // send push to players
        subscriptionService.getWorldPlayerUuids(session).forEach { uuid ->
            // build push
            val push = this.buildUseEmotePush(request, user)
            sessionService.getSession(uuid)?.pushEvent(push)
        }
        return WebsocketEmoteV1.UseEmoteResponse.newBuilder().apply {
            this.emoteId = request.emoteId
            this.emoteMetadata = request.emoteMetadata
            this.status = WebsocketEmoteV1.UseEmoteResponse_Status.USEEMOTERESPONSE_STATUS_STATUS_OK
        }.build()
    }

    override suspend fun processStopEmote(
        session: WebSocketSession,
        user: User
    ): WebsocketEmoteV1.StopEmoteResponse {
        subscriptionService.getWorldPlayerUuids(session).forEach { uuid ->
            // build push
            val push = this.buildStopEmotePush(user)
            sessionService.getSession(uuid)?.pushEvent(push)
        }
        return WebsocketEmoteV1.StopEmoteResponse.getDefaultInstance()
    }

    private fun buildUseEmotePush(request: WebsocketEmoteV1.UseEmoteRequest, user: User) =
        WebsocketEmoteV1.UseEmotePush.newBuilder().apply {
            this.emoteId = request.emoteId
            this.emoteMetadata = request.emoteMetadata
            this.playerUuid = user.uuid.toLunarClientUUID()
            this.emoteSoundtrackUrl = request.emoteSoundtrackUrl
        }.build()

    private fun buildStopEmotePush(user: User) = WebsocketEmoteV1.StopEmotePush.newBuilder().apply {
        this.playerUuid = user.uuid.toLunarClientUUID()
    }.build()
}