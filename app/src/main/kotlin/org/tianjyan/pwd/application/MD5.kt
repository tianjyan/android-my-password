package org.tianjyan.pwd.application

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5 {
    private val NUMBER_0XFF = 0xFF
    private val NUMBER_0X10 = 0x10

    fun getMD5(value: String): String {
        val hash: ByteArray
        try {
            hash = MessageDigest.getInstance("MD5").digest(value.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Huh, MD5 should be supported?", e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Huh, UTF-8 should be supported?", e)
        }

        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b.toInt() and NUMBER_0XFF < NUMBER_0X10) hex.append("0")
            hex.append(Integer.toHexString(b.toInt() and NUMBER_0XFF))
        }
        return hex.toString()
    }
}