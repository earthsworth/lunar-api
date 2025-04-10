package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.jam.v1.LoginResponse
import com.lunarclient.websocket.jam.v1.OwnedJam
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.repository.SongRepository
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.service.SongMapper
import org.cubewhy.celestial.util.toProtobufType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Instant

@Service
class JamServiceImpl(
    private val songRepository: SongRepository,
    private val songMapper: SongMapper,
    private val scope: CoroutineScope,
) : JamService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun init() {
        // add default songs
        scope.launch {
            if (songRepository.count().awaitFirst() == 0L) {
                val defaultSongs = listOf(
                    Song(
                        id = 4,
                        fakeStyngrId = "24071783-7969-497a-9e4e-1551621b82a9",
                        name = "I got the power of flight, I move the speed of light",
                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
                        song = "I Feel Alive",
                        artist = "Jack Black",
                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
                        durationMillis = 14600
                    ),
                    Song(
                        id = 5,
                        fakeStyngrId = "65d691b4-0f0d-4575-8c17-e93776f17774",
                        name = "Cause I feel alive, I move mountains with my mind",
                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
                        song = "I Feel Alive",
                        artist = "Jack Black",
                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
                        durationMillis = 23580
                    ),
                    Song(
                        id = 6,
                        fakeStyngrId = "3ba4289f-306e-41f7-b526-c683d034f01a",
                        name = "La-la-la-lava. Ch-ch-ch-chicken. Steve's Lava Chicken, yeah it's tasty as hell",
                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
                        song = "Lava Chicken",
                        artist = "Jack Black",
                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
                        durationMillis = 17680
                    ),
                    Song(
                        id = 7,
                        fakeStyngrId = "b91f1515-b01a-48d0-9d84-461cc1b2459d",
                        name = "I needed some stuff with some bop in it",
                        image = "https://musicassets.lunarclientprod.com/file/f60105ee3b8d64a54ac6ae43d0b1b944.jpeg",
                        song = "Bop",
                        artist = "DaBaby",
                        album = "Kirk",
                        durationMillis = 13500
                    ),
                    Song(
                        id = 8,
                        fakeStyngrId = "b90282a1-5589-4712-b89e-a7c41c9f577c",
                        name = "Never gonna give you up, never gonna let you down",
                        image = "https://musicassets.lunarclientprod.com/file/f40a3fcac04390f67758215eff3aaea2.jpeg",
                        song = "Never Gonna Give You Up",
                        artist = "Rick Astley",
                        album = "The Best Of Me",
                        durationMillis = 19330
                    ),
                    Song(
                        id = 9,
                        fakeStyngrId = "ac0c92c8-d11b-4885-a426-a0d5c639816c",
                        name = "Wake me up before you go-go. Don't leave me hanging on like a yo-yo",
                        image = "https://musicassets.lunarclientprod.com/file/8efa869c0609c64e942b59acdaf6c987.jpeg",
                        song = "Wake Me Up Before You Go Go",
                        artist = "Wham!",
                        album = "Make It Big",
                        durationMillis = 20970
                    ),
                    Song(
                        id = 10,
                        fakeStyngrId = "39ee69fa-9182-4441-8d34-228881173a29",
                        name = "Whether you're a brother or whether you're a mother. You're stayin' alive",
                        image = "https://musicassets.lunarclientprod.com/file/13483404429923e850d9048f1d7414dd.jpeg",
                        song = "Stayin Alive",
                        artist = "Bee Gees",
                        album = "Greatest",
                        durationMillis = 25990
                    ),
                    Song(
                        id = 11,
                        fakeStyngrId = "c91e6fcb-5911-40e0-8d91-d22d9ed8dbab",
                        name = "Soulja Boy off in it, oh. Watch me crank it, watch me roll",
                        image = "https://musicassets.lunarclientprod.com/file/69f25d939a6b8de1fd3eb3dc37f743ad.jpeg",
                        song = "Crank That",
                        artist = "Soulja Boy",
                        album = "Souljaboytellem.com",
                        durationMillis = 14940
                    ),
                    Song(
                        id = 12,
                        fakeStyngrId = "e60ba800-3efe-4fc8-b5f3-8180c6e78201",
                        name = "I don't want no trouble and trouble don't want me",
                        image = "https://musicassets.lunarclientprod.com/file/4f63c64a8ac528838bb61ede6ddc5bbe.jpeg",
                        song = "We Fresh",
                        artist = "Mannie Fresh",
                        album = "The Mind Of Mannie Fresh",
                        durationMillis = 25490
                    ),
                    Song(
                        id = 13,
                        fakeStyngrId = "5b4095b8-d4d3-4d77-929f-b5b1f25f5ade",
                        name = "Another head aches, another heart breaks, I'm so much older than I can take",
                        image = "https://musicassets.lunarclientprod.com/file/e0361b0cbadbd6ff276370f62abfdc5c.jpeg",
                        song = "All These Things That I've Done",
                        artist = "The Killers",
                        album = "Direct Hits",
                        durationMillis = 22770
                    ),
                    Song(
                        id = 14,
                        fakeStyngrId = "df362ae5-dd73-4095-98e7-0c0a8d572216",
                        name = "Oh I feel good, oh I feel good",
                        image = "https://musicassets.lunarclientprod.com/file/c3c116617aa22331f877f0b1780d9019.jpeg",
                        song = "I Feel Good",
                        artist = "2 Chainz",
                        album = "Based On A T.R.U. Story (Deluxe)",
                        durationMillis = 10810
                    ),
                    Song(
                        id = 15,
                        fakeStyngrId = "07ad9c9e-baca-4c9d-8cc4-fab6d9f0108b",
                        name = "I'd rather die than to listen to you, my DNA not for imitation",
                        image = "https://musicassets.lunarclientprod.com/file/7a1f903f835f9801f99258e8f65d9dad.jpeg",
                        song = "DNA.",
                        artist = "Kendrick Lamar",
                        album = "Damn.",
                        durationMillis = 22020
                    ),
                    Song(
                        id = 16,
                        fakeStyngrId = "cc4e082b-cdf3-43e9-a6ab-74735a71c51b",
                        name = "I got, I got, I got, I got loyalty, got royalty inside my DNA",
                        image = "https://musicassets.lunarclientprod.com/file/7a1f903f835f9801f99258e8f65d9dad.jpeg",
                        song = "Dna.",
                        artist = "Kendrick Lamar",
                        album = "Damn.",
                        durationMillis = 29120
                    ),
                    Song(
                        id = 17,
                        fakeStyngrId = "51470071-1480-4415-a83a-3f29185a381d",
                        name = "Wanna look like me. Wanna be in demand, get booked like me",
                        image = "https://musicassets.lunarclientprod.com/file/7ae5af5772be41a1505137ea9b100320.jpeg",
                        song = "Barbie Tingz",
                        artist = "Nicki Minaj",
                        album = "Queen",
                        durationMillis = 27560
                    ),
                    Song(
                        id = 18,
                        fakeStyngrId = "bf9bccaa-a149-433d-bfa2-cbce7c45cb7f",
                        name = "That's on me, that's on me, I know",
                        image = "https://musicassets.lunarclientprod.com/file/13a30fc6e0777874f0850bdb22a3d816.jpeg",
                        song = "That's On Me",
                        artist = "Mac Miller",
                        album = "Circles",
                        durationMillis = 27970
                    ),
                    Song(
                        id = 19,
                        fakeStyngrId = "fc5ddda2-e93d-4696-9064-eec5cff8c961",
                        name = "Hold on, drink freely and holla at me if you need me",
                        image = "https://musicassets.lunarclientprod.com/file/1f021f65324e8fdcd6815aa342a9f870.jpeg",
                        song = "Enjoy Yourself",
                        artist = "Pop Smoke",
                        album = "Shoot For The Stars Aim For The Moon",
                        durationMillis = 17110
                    ),
                    Song(
                        id = 20,
                        fakeStyngrId = "ff518aab-2166-4683-ae17-0b8fd216ea1a",
                        name = "Just Can't Get Enough Instrumental",
                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
                        song = "Just Can't Get Enough",
                        artist = "Jamieson Shaw",
                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
                        durationMillis = 15980
                    ),
                    Song(
                        id = 21,
                        fakeStyngrId = "9d8fea42-a20c-4b52-9521-056407498627",
                        name = "I wanna be free, I wanna be free, ah",
                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
                        song = "Change Song",
                        artist = "Dayglow",
                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
                        durationMillis = 14570
                    ),
                    Song(
                        id = 22,
                        fakeStyngrId = "d8d13c2f-220b-4527-903d-e077a065d66f",
                        name = "I got no shelter in the driving rain, I got no lady to ease my pain",
                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
                        song = "When I'm Gone",
                        artist = "Dirty Honey",
                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
                        durationMillis = 29980
                    ),
                )
                logger.info { "Add default songs (${defaultSongs.size} songs)" }
                songRepository.saveAll(defaultSongs).awaitLast()
            }
        }
    }

    override suspend fun process(
        method: String,
        payload: ByteString,
        session: WebSocketSession,
        user: User
    ): WebsocketResponse {
        return when (method) {
            "Login" -> this.processLogin(session, user)
            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processLogin(session: WebSocketSession, user: User): WebsocketResponse {
        val response = LoginResponse.newBuilder().apply {
            // find available songs
            this.addAllOwnedJams(
                songRepository.findAll().map { this@JamServiceImpl.buildJam(it) }.collectList().awaitLast()
            )
        }.build().toWebsocketResponse()
        return response
    }

    private fun buildJam(song: Song): OwnedJam {
        return OwnedJam.newBuilder().apply {
            this.jamId = song.id.toInt()
            this.grantedAt = Instant.now().toProtobufType()
        }.build()
    }

    override suspend fun availableSongs(): List<SongVO> {
        return songRepository.findAll().map { song ->
            songMapper.mapToSongVO(song)
        }.collectList().awaitLast()
    }
}