package org.tianjyan.pwd.application

import android.annotation.SuppressLint
import android.util.Base64
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

object AES {
    private val AES = "AES"
    private val KEYSIZE = 128

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, IllegalBlockSizeException::class, InvalidKeyException::class, BadPaddingException::class, NoSuchPaddingException::class)
    fun encrypt(cleartext: String, seed: String): String {
        val rawKey = getRawKey(seed.toByteArray())
        val result = encrypt(rawKey, cleartext.toByteArray())
        return Base64.encodeToString(result, 0)
    }

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, IllegalBlockSizeException::class, InvalidKeyException::class, BadPaddingException::class, NoSuchPaddingException::class)
    fun decrypt(encrypted: String, seed: String): String {
        val rawKey = getRawKey(seed.toByteArray())
        val enc = Base64.decode(encrypted, 0)
        val result = decrypt(rawKey, enc)
        return String(result)
    }

    @SuppressLint("TrulyRandom")
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    private fun getRawKey(seed: ByteArray): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(AES)
        val sr = SecureRandom.getInstance("SHA1PRNG", "Crypto")
        sr.setSeed(seed)
        keyGenerator.init(KEYSIZE, sr)
        val secretKey = keyGenerator.generateKey()
        return secretKey.encoded
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    private fun encrypt(raw: ByteArray, clear: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(raw, AES)
        val cipher = Cipher.getInstance(AES)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        return cipher.doFinal(clear)
    }

    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class)
    private fun decrypt(raw: ByteArray, encrypted: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(raw, AES)
        val cipher = Cipher.getInstance(AES)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipher.doFinal(encrypted)
    }
}