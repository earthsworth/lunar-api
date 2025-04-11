package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.jam.v1.LoginResponse
import com.lunarclient.websocket.jam.v1.OwnedJam
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.dto.CreateSongDTO
import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrSongVO
import org.cubewhy.celestial.repository.SongRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.service.SongMapper
import org.cubewhy.celestial.util.extractBaseUrl
import org.cubewhy.celestial.util.toProtobufType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.server.ServerWebExchange
import java.time.Instant

@Service
class JamServiceImpl(
    private val songRepository: SongRepository,
    private val songMapper: SongMapper,
    private val scope: CoroutineScope,
    private val userRepository: UserRepository,
) : JamService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

//    @PostConstruct
//    private fun init() {
//        // add default songs
//        scope.launch {
//            if (songRepository.count().awaitFirst() == 0L) {
//                val defaultSongs = listOf(
//                    Song(
//                        name = "I got the power of flight, I move the speed of light",
//                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
//                        song = "I Feel Alive",
//                        artist = "Jack Black",
//                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
//                        durationMillis = 14600,
//                    ),
//                    Song(
//                        name = "Cause I feel alive, I move mountains with my mind",
//                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
//                        song = "I Feel Alive",
//                        artist = "Jack Black",
//                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
//                        durationMillis = 23580
//                    ),
//                    Song(
//                        name = "La-la-la-lava. Ch-ch-ch-chicken. Steve's Lava Chicken, yeah it's tasty as hell",
//                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
//                        song = "Lava Chicken",
//                        artist = "Jack Black",
//                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
//                        durationMillis = 17680
//                    ),
//                    Song(
//                        name = "I needed some stuff with some bop in it",
//                        image = "https://musicassets.lunarclientprod.com/file/f60105ee3b8d64a54ac6ae43d0b1b944.jpeg",
//                        song = "Bop",
//                        artist = "DaBaby",
//                        album = "Kirk",
//                        durationMillis = 13500
//                    ),
//                    Song(
//                        name = "Never gonna give you up, never gonna let you down",
//                        image = "https://musicassets.lunarclientprod.com/file/f40a3fcac04390f67758215eff3aaea2.jpeg",
//                        song = "Never Gonna Give You Up",
//                        artist = "Rick Astley",
//                        album = "The Best Of Me",
//                        durationMillis = 19330
//                    ),
//                    Song(
//                        name = "Wake me up before you go-go. Don't leave me hanging on like a yo-yo",
//                        image = "https://musicassets.lunarclientprod.com/file/8efa869c0609c64e942b59acdaf6c987.jpeg",
//                        song = "Wake Me Up Before You Go Go",
//                        artist = "Wham!",
//                        album = "Make It Big",
//                        durationMillis = 20970
//                    ),
//                    Song(
//                        name = "Whether you're a brother or whether you're a mother. You're stayin' alive",
//                        image = "https://musicassets.lunarclientprod.com/file/13483404429923e850d9048f1d7414dd.jpeg",
//                        song = "Stayin Alive",
//                        artist = "Bee Gees",
//                        album = "Greatest",
//                        durationMillis = 25990
//                    ),
//                    Song(
//                        name = "Soulja Boy off in it, oh. Watch me crank it, watch me roll",
//                        image = "https://musicassets.lunarclientprod.com/file/69f25d939a6b8de1fd3eb3dc37f743ad.jpeg",
//                        song = "Crank That",
//                        artist = "Soulja Boy",
//                        album = "Souljaboytellem.com",
//                        durationMillis = 14940
//                    ),
//                    Song(
//                        name = "I don't want no trouble and trouble don't want me",
//                        image = "https://musicassets.lunarclientprod.com/file/4f63c64a8ac528838bb61ede6ddc5bbe.jpeg",
//                        song = "We Fresh",
//                        artist = "Mannie Fresh",
//                        album = "The Mind Of Mannie Fresh",
//                        durationMillis = 25490
//                    ),
//                    Song(
//                        name = "Another head aches, another heart breaks, I'm so much older than I can take",
//                        image = "https://musicassets.lunarclientprod.com/file/e0361b0cbadbd6ff276370f62abfdc5c.jpeg",
//                        song = "All These Things That I've Done",
//                        artist = "The Killers",
//                        album = "Direct Hits",
//                        durationMillis = 22770
//                    ),
//                    Song(
//                        name = "Oh I feel good, oh I feel good",
//                        image = "https://musicassets.lunarclientprod.com/file/c3c116617aa22331f877f0b1780d9019.jpeg",
//                        song = "I Feel Good",
//                        artist = "2 Chainz",
//                        album = "Based On A T.R.U. Story (Deluxe)",
//                        durationMillis = 10810
//                    ),
//                    Song(
//                        name = "I'd rather die than to listen to you, my DNA not for imitation",
//                        image = "https://musicassets.lunarclientprod.com/file/7a1f903f835f9801f99258e8f65d9dad.jpeg",
//                        song = "DNA.",
//                        artist = "Kendrick Lamar",
//                        album = "Damn.",
//                        durationMillis = 22020
//                    ),
//                    Song(
//                        name = "I got, I got, I got, I got loyalty, got royalty inside my DNA",
//                        image = "https://musicassets.lunarclientprod.com/file/7a1f903f835f9801f99258e8f65d9dad.jpeg",
//                        song = "Dna.",
//                        artist = "Kendrick Lamar",
//                        album = "Damn.",
//                        durationMillis = 29120
//                    ),
//                    Song(
//                        name = "Wanna look like me. Wanna be in demand, get booked like me",
//                        image = "https://musicassets.lunarclientprod.com/file/7ae5af5772be41a1505137ea9b100320.jpeg",
//                        song = "Barbie Tingz",
//                        artist = "Nicki Minaj",
//                        album = "Queen",
//                        durationMillis = 27560
//                    ),
//                    Song(
//                        name = "That's on me, that's on me, I know",
//                        image = "https://musicassets.lunarclientprod.com/file/13a30fc6e0777874f0850bdb22a3d816.jpeg",
//                        song = "That's On Me",
//                        artist = "Mac Miller",
//                        album = "Circles",
//                        durationMillis = 27970
//                    ),
//                    Song(
//                        name = "Hold on, drink freely and holla at me if you need me",
//                        image = "https://musicassets.lunarclientprod.com/file/1f021f65324e8fdcd6815aa342a9f870.jpeg",
//                        song = "Enjoy Yourself",
//                        artist = "Pop Smoke",
//                        album = "Shoot For The Stars Aim For The Moon",
//                        durationMillis = 17110
//                    ),
//                    Song(
//                        name = "Just Can't Get Enough Instrumental",
//                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
//                        song = "Just Can't Get Enough",
//                        artist = "Jamieson Shaw",
//                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
//                        durationMillis = 15980
//                    ),
//                    Song(
//                        name = "I wanna be free, I wanna be free, ah",
//                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
//                        song = "Change Song",
//                        artist = "Dayglow",
//                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
//                        durationMillis = 14570
//                    ),
//                    Song(
//                        name = "I got no shelter in the driving rain, I got no lady to ease my pain",
//                        image = "https://musicassets.lunarclientprod.com/file/530149976ca0d29c19a2ec002d04508e.jpeg",
//                        song = "When I'm Gone",
//                        artist = "Dirty Honey",
//                        album = "A Minecraft Movie (Original Motion Picture Soundtrack)",
//                        durationMillis = 29980
//                    ),
//                )
//                logger.info { "Add default songs (${defaultSongs.size} songs)" }
//                songRepository.saveAll(defaultSongs).awaitLast()
//            }
//        }
//    }

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

    override suspend fun styngrPlaySong(songId: String, baseUrl: String): StyngrSongVO {
        // find song
        logger.info { "Request song $songId" }
        val song = songRepository.findById(songId).awaitFirstOrNull()
            ?: throw IllegalStateException("Song with id $songId not found")
        return StyngrSongVO("${baseUrl}api/upload?id=${song.uploadId}")
    }

    override suspend fun createSong(dto: CreateSongDTO, authentication: Authentication): SongVO {
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        // check contentType
        songRepository
        val song = Song(
            user = user.id!!,
            name = dto.name,
            thumbnail = dto.thumbnail,
            songName = dto.songName,
            artist = dto.artist,
            album = dto.album,
            durationMillis = dto.durationMillis,
            uploadId = dto.uploadId
        )
        // save the song
        logger.info { "Song ${song.name} was created by user ${user.username}" }
        return songMapper.mapToSongVO(songRepository.save(song).awaitFirst())
    }

    private fun buildJam(song: Song): OwnedJam {
        return OwnedJam.newBuilder().apply {
            this.jamId = song.numberId
            this.grantedAt = Instant.now().toProtobufType()
        }.build()
    }

    override suspend fun availableSongs(exchange: ServerWebExchange): List<LunarSongVO> {
        return songRepository.findAll().map { song ->
            songMapper.mapToLunarSongVO(song, exchange.extractBaseUrl())
        }.collectList().awaitLast()
    }
}