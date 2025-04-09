package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import com.lunarclient.websocket.emote.v1.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.cubewhy.celestial.entity.User
import org.cubewhy.celestial.entity.WebsocketResponse
import org.cubewhy.celestial.entity.emptyWebsocketResponse
import org.cubewhy.celestial.entity.toWebsocketResponse
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.EmoteService
import org.cubewhy.celestial.service.SessionService
import org.cubewhy.celestial.service.SubscriptionService
import org.cubewhy.celestial.util.toLunarClientUUID
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class EmoteServiceImpl(
    private val sessionService: SessionService,
    private val subscriptionService: SubscriptionService,
    private val userRepository: UserRepository
) : EmoteService {

//    companion object {
//        private val logger = KotlinLogging.logger {}
//    }

//    private val emoteList = mutableListOf<Emote>()
//
//    @PostConstruct
//    private fun init() {
//        logger.info { "Loading emotes from csv" }
//        val resource = ClassPathResource("emote/emotes.csv")
//        // load emotes
//        resource.inputStream.use { inputStream ->
//            InputStreamReader(inputStream).use { reader ->
//                CSVReader(reader).use { csvReader ->
//                    csvReader.forEach { row ->
//                        if (row.size >= 4) {
//                            val id = row[0].trim().toInt()
//                            val name = row[1].trim()
//                            emoteList.add(Emote(id, name))
//                        }
//                    }
//                }
//            }
//        }
//        logger.info { "Loaded ${emoteList.size} emotes" }
//    }

    override fun refreshEmote(user: User) {
        sessionService.push(user, RefreshEmotesPush.getDefaultInstance())
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "Login" -> this.processLogin(user).toWebsocketResponse()
            "UseEmote" -> this.processUseEmote(
                UseEmoteRequest.parseFrom(payload),
                session,
                user
            ).toWebsocketResponse()

            "StopEmote" -> this.processStopEmote(
                session,
                user
            ).toWebsocketResponse()

            "UpdateEquippedEmotes" -> this.processUpdateEquippedEmotes(
                UpdateEquippedEmotesRequest.parseFrom(payload),
                session,
                user
            ).toWebsocketResponse()

            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processUpdateEquippedEmotes(
        request: UpdateEquippedEmotesRequest,
        session: WebSocketSession,
        user: User
    ): UpdateEquippedEmotesResponse {
        user.emote.equippedEmoteIds = request.equippedEmoteIdsList
        userRepository.save(user).awaitFirst()
        return UpdateEquippedEmotesResponse.newBuilder().build()
    }

    override suspend fun processLogin(user: User): GeneratedMessage {
        return LoginResponse.newBuilder().apply {
//            addAllOwnedEmotes(emoteList.map { it.toOwnedEmote(it.emoteId) })
//            addAllOwnedEmoteIds(emoteList.map { it.emoteId })
            addAllEquippedEmoteIds(user.emote.equippedEmoteIds)
            // hack: use LunarClient's hasAllEmotesFlag
            hasAllEmotesFlag = true
        }.build()
    }

    override suspend fun processUseEmote(
        request: UseEmoteRequest,
        session: WebSocketSession,
        user: User
    ): UseEmoteResponse {
        // build push
        val push = this.buildUseEmotePush(request, user)
        subscriptionService.getWorldPlayerUuids(session).forEach { uuid ->
            userRepository.findByUuid(uuid).awaitFirstOrNull()?.let { target ->
                // push to players
                sessionService.push(target, push)
            }
        }
        return UseEmoteResponse.newBuilder().apply {
            this.emoteId = request.emoteId
            this.emoteMetadata = request.emoteMetadata
            this.status = UseEmoteResponse.Status.STATUS_OK
        }.build()
    }

    override suspend fun processStopEmote(
        session: WebSocketSession,
        user: User
    ): StopEmoteResponse {
        val push = this.buildStopEmotePush(user)
        subscriptionService.getWorldPlayerUuids(session).forEach { uuid ->
            // find target
            userRepository.findByUuid(uuid).awaitFirstOrNull()?.let { target ->
                sessionService.push(target, push)
            }
        }
        return StopEmoteResponse.getDefaultInstance()
    }

    private fun buildUseEmotePush(request: UseEmoteRequest, user: User) =
        UseEmotePush.newBuilder().apply {
            this.emoteId = request.emoteId
            this.emoteMetadata = request.emoteMetadata
            this.playerUuid = user.uuid.toLunarClientUUID()
            this.emoteSoundtrackUrl = request.emoteSoundtrackUrl
        }.build()

    private fun buildStopEmotePush(user: User) = StopEmotePush.newBuilder().apply {
        this.playerUuid = user.uuid.toLunarClientUUID()
    }.build()
}