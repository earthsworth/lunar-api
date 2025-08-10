package org.cubewhy.celestial.service.impl

import com.google.protobuf.ByteString
import com.lunarclient.websocket.jam.v1.LoginResponse
import com.lunarclient.websocket.jam.v1.OwnedJam
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.cubewhy.celestial.entity.*
import org.cubewhy.celestial.entity.dto.CreateSongDTO
import org.cubewhy.celestial.entity.dto.ModifySongDTO
import org.cubewhy.celestial.entity.vo.LunarSongVO
import org.cubewhy.celestial.entity.vo.SongVO
import org.cubewhy.celestial.entity.vo.styngr.StyngrSongVO
import org.cubewhy.celestial.protocol.ClientConnection
import org.cubewhy.celestial.repository.SongRepository
import org.cubewhy.celestial.repository.UploadRepository
import org.cubewhy.celestial.repository.UserRepository
import org.cubewhy.celestial.service.JamService
import org.cubewhy.celestial.service.SongMapper
import org.cubewhy.celestial.util.toProtobufType
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class JamServiceImpl(
    private val songRepository: SongRepository,
    private val songMapper: SongMapper,
    private val userRepository: UserRepository,
    private val uploadRepository: UploadRepository,
) : JamService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val serviceName: String = "lunarclient.websocket.jam.v1.JamService"

    override suspend fun process(
        method: String,
        payload: ByteString,
        connection: ClientConnection<*>,
        user: User
    ): RpcResponse {
        return when (method) {
            "Login" -> this.processLogin(connection, user)
            else -> emptyWebsocketResponse()
        }
    }

    override suspend fun processLogin(connection: ClientConnection<*>, user: User): RpcResponse {
        val response = LoginResponse.newBuilder().apply {
            // find available songs
            this.addAllOwnedJams(
                songRepository.findAll().map { this@JamServiceImpl.buildJam(it) }.collectList()
                    .awaitLast()
            )
        }.build().toWebsocketResponse()
        return response
    }

    override suspend fun listOwn(authentication: Authentication, baseUrl: String): List<SongVO> {
        // find user
        val user = userRepository.findByUsername(authentication.name).awaitFirstOrNull() ?: return emptyList()
        // find songs
        val songs = songRepository.findAllByOwner(user.id!!).collectList().awaitFirst()
        return songs.map { songMapper.mapToSongVO(it, baseUrl) }
    }

    override suspend fun styngrPlaySong(songId: String, baseUrl: String): StyngrSongVO {
        // find song
        logger.debug { "Request song $songId" }
        // parse uuid
        val parsedUuid = UUID.fromString(songId)
        val song = songRepository.findByUuid(parsedUuid).awaitFirstOrNull()
            ?: throw IllegalStateException("Song with uuid $songId not found")
        return StyngrSongVO(
            url = if (song.remoteFileUrl != null) song.remoteFileUrl!! else "${baseUrl}api/upload?id=${song.uploadId}",
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS).toString()
        )
    }

    override suspend fun createSong(dto: CreateSongDTO, authentication: Authentication, baseUrl: String): SongVO {
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        // check contentType
        val thumbnailUpload = uploadRepository.findById(dto.thumbnail).awaitFirstOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad thumbnail upload id")
        if (!thumbnailUpload.contentType.startsWith("image/")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Thumbnail is not a image")
        }
        if (dto.uploadId != null) {
            val songUpload = uploadRepository.findById(dto.uploadId).awaitFirstOrNull()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad song upload id")
            if (songUpload.contentType != "audio/x-wav" && songUpload.contentType != "audio/vnd.wave") {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad song content type, only wave files allowed")
            }
        }

        if (dto.uploadId == null && dto.remoteFileUrl == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided")
        }

        val song = Song(
            owner = user.id!!,
            name = dto.name,
            thumbnail = dto.thumbnail,
            songName = dto.songName,
            artist = dto.artist,
            album = dto.album,
            durationMillis = dto.durationMillis,
            uploadId = dto.uploadId,
            remoteFileUrl = dto.remoteFileUrl
        )
        // save the song
        logger.info { "Song ${song.name} was created by user ${user.username}" }
        return songMapper.mapToSongVO(songRepository.save(song).awaitFirst(), baseUrl)
    }

    override suspend fun modifySong(dto: ModifySongDTO, authentication: Authentication, baseUrl: String): SongVO {
        // find song
        val song = songRepository.findById(dto.songId).awaitFirstOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Song with id ${dto.songId} not found")
        // check owner
        val user = userRepository.findByUsername(authentication.name).awaitFirst()
        if (song.owner != user.id!! && !user.roles.contains(Role.ADMIN)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You had no permission to edit this song")
        }

        if (dto.uploadId == null && dto.remoteFileUrl == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided")
        } else if (dto.uploadId != null && dto.remoteFileUrl != null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "uploadId and remoteFileUrl cannot be defined at the same time"
            )
        }

        song.apply {
            this.songName = dto.songName
            this.name = dto.name
            this.thumbnail = dto.thumbnail
            this.album = dto.album
            this.artist = dto.artist
            this.durationMillis = dto.durationMillis
            this.uploadId = dto.uploadId
            this.remoteFileUrl = dto.remoteFileUrl
        }
        // save song
        return songMapper.mapToSongVO(songRepository.save(song).awaitFirst(), baseUrl)
    }


    private fun buildJam(song: Song): OwnedJam {
        return OwnedJam.newBuilder().apply {
            this.jamId = song.numberId
            this.grantedAt = Instant.now().toProtobufType()
        }.build()
    }

    override suspend fun availableSongs(baseUrl: String): List<LunarSongVO> {
        return songRepository.findAll().map { song ->
            songMapper.mapToLunarSongVO(song, baseUrl)
        }.collectList().awaitLast()
    }
}