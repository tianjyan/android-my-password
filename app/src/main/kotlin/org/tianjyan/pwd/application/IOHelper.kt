package org.tianjyan.pwd.application

import org.apache.http.util.EncodingUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object IOHelper {

    @Throws(IOException::class)
    fun readSDFile(fileName: String): String {

        val file = File(fileName)

        val fis = FileInputStream(file)

        val length = fis.available()

        val buffer = ByteArray(length)
        fis.read(buffer)

        val res = EncodingUtils.getString(buffer, "UTF-8")

        fis.close()
        return res
    }


    @Throws(IOException::class)
    fun writeSDFile(fileName: String, writeStr: String) {

        val file = File(fileName)

        val fos = FileOutputStream(file)

        val bytes = writeStr.toByteArray()

        fos.write(bytes)

        fos.close()
    }

    fun deleteSDFile(fileName: String) {
        val file = File(fileName)
        file.delete()
    }
}