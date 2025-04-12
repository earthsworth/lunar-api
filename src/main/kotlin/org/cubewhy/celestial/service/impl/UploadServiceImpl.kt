package org.cubewhy.celestial.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.collect
import org.cubewhy.celestial.entity.Upload
import org.cubewhy.celestial.entity.config.LunarProperties
import org.cubewhy.celestial.entity.vo.UploadVO
import org.cubewhy.celestial.repository.UploadRepository
import org.cubewhy.celestial.service.UploadMapper
import org.cubewhy.celestial.service.UploadService
import org.cubewhy.celestial.util.parseSizeString
import org.cubewhy.celestial.util.responseFailure
import org.cubewhy.celestial.util.streamData
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.Path

@Service
class UploadServiceImpl(
    lunarProperties: LunarProperties,
    private val uploadRepository: UploadRepository,
    private val uploadMapper: UploadMapper
) : UploadService {
    companion object {
        val DATA_DIR = File("data")
        val UPLOAD_DIR = File(DATA_DIR, "uploads")
        private val logger = KotlinLogging.logger {}
    }

    private val maxUploadSize = parseSizeString(lunarProperties.upload.maxSize)

    @PostConstruct
    private fun init() {
        if (!UPLOAD_DIR.exists()) {
            // create dirs
            logger.info { "Creating dirs" }
            UPLOAD_DIR.mkdirs()
        }
    }

    override suspend fun upload(exchange: ServerWebExchange): UploadVO {
        val contentLength = (exchange.request.headers.getFirst(HttpHeaders.CONTENT_LENGTH)
            ?: throw IllegalArgumentException("Bad request")).toInt()
        val contentType = exchange.request.headers.getFirst(HttpHeaders.CONTENT_TYPE) ?: throw IllegalArgumentException(
            "No content type header provided"
        )
        if (contentLength > maxUploadSize) {
            throw IllegalArgumentException("Bad request: $contentLength > maxUploadSize: $maxUploadSize")
        }
        val digest = MessageDigest.getInstance("SHA-256")
        // receive file
        val byteArray = exchange.request.body
            .publishOn(Schedulers.boundedElastic())
            .concatMap { it.asInputStream().readAllBytes().toMono() }
            .reduce(ByteArray(0)) { acc, bytes -> acc + bytes }
            .awaitFirst()
        digest.update(byteArray)
        // calc sha256
        val sha256Hash = digest.digest().joinToString("") { "%02x".format(it) }
        // check is exist
        val existUpload = uploadRepository.findBySha256(sha256Hash).awaitFirstOrNull()
        if (existUpload != null) {
            return uploadMapper.mapToUploadVO(existUpload)
        }
        logger.info { "Receiving file with hash $sha256Hash" }
        // save to local
        this.saveFile(sha256Hash, byteArray)
        return uploadMapper.mapToUploadVO(
            uploadRepository.save(
                Upload(
                    sha256 = sha256Hash,
                    contentType = contentType
                )
            ).awaitFirst()
        )
    }

    override suspend fun download(uploadId: String, exchange: ServerWebExchange) {
        // find upload
        val upload = uploadRepository.findById(uploadId).awaitFirstOrNull()
        if (upload == null) {
            exchange.responseFailure(404, "Upload not found").awaitFirstOrNull()
            return
        }
        // get file & open as buffer
        logger.debug { "Streaming download $uploadId" }
        val publisher =
            DataBufferUtils.read(Path(UPLOAD_DIR.absolutePath, upload.sha256), DefaultDataBufferFactory(), 4096)
        exchange.streamData(publisher).awaitFirstOrNull()
    }

    private fun saveFile(hash: String, bytes: ByteArray) {
//        FileChannel.open(
//            Path(UPLOAD_DIR.absolutePath, hash),
//            StandardOpenOption.CREATE,
//            StandardOpenOption.WRITE,
//            StandardOpenOption.TRUNCATE_EXISTING
//        ).use { channel ->
//            buffer.rewind()
//            while (buffer.hasRemaining()) {
//                channel.write(buffer)
//            }
//        }
        val file = File(UPLOAD_DIR, hash)
        file.outputStream().use { it.write(bytes) }
    }
}