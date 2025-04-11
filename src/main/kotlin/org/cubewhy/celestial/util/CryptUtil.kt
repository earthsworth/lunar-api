package org.cubewhy.celestial.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.UnsupportedEncodingException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object CryptUtil {
    private val logger = KotlinLogging.logger {}

    /**
     * Generate a new shared secret AES key from a secure random source
     */
    fun createNewSharedKey(): SecretKey {
        return try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(128)
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw Error(e)
        }
    }

    /**
     * Generates RSA KeyPair
     */
    fun generateKeyPair(): KeyPair? {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(1024)
            keyPairGenerator.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            logger.error { "Failed to generate KeyPair" }
            null
        }
    }

    /**
     * Compute a serverId hash for use by sendSessionRequest()
     */
    fun getServerIdHash(serverId: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray? {
        return try {
            digestOperation("SHA-1", serverId.toByteArray(charset("ISO_8859_1")), secretKey.encoded, publicKey.encoded)
        } catch (e: UnsupportedEncodingException) {
            logger.error(e) { "Failed to generate server id" }
            null
        }
    }

    /**
     * Compute a message digest on arbitrary byte[] data
     */
    private fun digestOperation(algorithm: String, vararg data: ByteArray): ByteArray? {
        return try {
            val messageDigest = MessageDigest.getInstance(algorithm)
            for (bytes in data) {
                messageDigest.update(bytes)
            }
            messageDigest.digest()
        } catch (e: NoSuchAlgorithmException) {
            logger.error(e) { "Error while generating key" }
            null
        }
    }

    /**
     * Create a new PublicKey from encoded X.509 data
     */
    fun decodePublicKey(encodedKey: ByteArray): PublicKey? {
        return try {
            val encodedKeySpec = X509EncodedKeySpec(encodedKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(encodedKeySpec)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: InvalidKeySpecException) {
            null
        }
    }

    /**
     * Decrypt shared secret AES key using RSA private key
     */
    fun decryptSharedKey(key: PrivateKey, secretKeyEncrypted: ByteArray): SecretKey {
        return SecretKeySpec(decryptData(key, secretKeyEncrypted), "AES")
    }

    /**
     * Encrypt byte[] data with RSA public key
     */
    fun encryptData(key: Key, data: ByteArray): ByteArray? {
        return cipherOperation(Cipher.ENCRYPT_MODE, key, data)
    }

    /**
     * Decrypt byte[] data with RSA private key
     */
    fun decryptData(key: Key, data: ByteArray): ByteArray? {
        return cipherOperation(Cipher.DECRYPT_MODE, key, data)
    }

    /**
     * Encrypt or decrypt byte[] data using the specified key
     */
    private fun cipherOperation(opMode: Int, key: Key, data: ByteArray): ByteArray? {
        return try {
            createTheCipherInstance(opMode, key.algorithm, key).doFinal(data)
        } catch (e: IllegalBlockSizeException) {
            logger.error(e) { "Cipher data failed!" }
            null
        } catch (e: BadPaddingException) {
            logger.error(e) { "Cipher data failed!" }
            null
        }
    }

    /**
     * Creates the Cipher Instance.
     */
    private fun createTheCipherInstance(opMode: Int, transformation: String, key: Key): Cipher {
        return try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(opMode, key)
            cipher
        } catch (e: InvalidKeyException) {
            logger.error(e) { "Cipher creation failed!" }
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            logger.error(e) { "Cipher creation failed!" }
            throw RuntimeException(e)
        } catch (e: NoSuchPaddingException) {
            logger.error(e) { "Cipher creation failed!" }
            throw RuntimeException(e)
        }
    }

    /**
     * Creates a Cipher instance using the AES/CFB8/NoPadding algorithm. Used for protocol encryption.
     */
    fun createNetCipherInstance(opMode: Int, key: Key): Cipher {
        return try {
            val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
            cipher.init(opMode, key, IvParameterSpec(key.encoded))
            cipher
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }
    }
}

fun generateRandomBytes(length: Int = 16): ByteArray {
    val random = SecureRandom()
    val bytes = ByteArray(length)
    random.nextBytes(bytes)
    return bytes
}