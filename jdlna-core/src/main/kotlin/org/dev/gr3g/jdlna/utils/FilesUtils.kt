package org.dev.gr3g.jdlna.utils

import org.seamless.util.io.Base64Coder
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object FilesUtils {

    /** Logger.  */
    private val logger = Logger
            .getLogger(FilesUtils::class.java.name)

    fun init() {
        val jdlna = Paths.get(System.getenv("INSTALL_DIR") ?: "/data/jdlna/")
        val logs = Paths.get(System.getenv("LOG_DIR") ?: "/var/log/jdlna/")
        try {
            if (!Files.exists(jdlna)) {
                Files.createDirectory(jdlna)
            }
            if (!Files.exists(logs)) {
                Files.createDirectory(logs)
            }
        } catch (exc: IOException) {
            logger.log(Level.SEVERE, "Error user home", exc)
        }
    }

    fun generateID(path: String, size: Long): Int {
        var code = 0
        try {
            val concatenation = path + size
            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest
                    .digest(concatenation.toByteArray(StandardCharsets.UTF_8))
            val str = Base64Coder.encodeBytes(hash)
            code = str.hashCode()
        } catch (exc: NoSuchAlgorithmException) {
            logger.log(Level.SEVERE, "Error generateID", exc)
        }
        return code
    }

    fun getFile(path: String): InputStream {
        return FilesUtils::class.java.getResourceAsStream(path)
    }

    @Throws(IOException::class)
    fun getFileContent(path: String): String {
        val input = getFile(path)
        val reader = BufferedReader(
                InputStreamReader(input, StandardCharsets.UTF_8))
        val builder = StringBuilder()
        reader.useLines {
            it.map { line ->
                builder.append(line)
            }
        }
        return builder.toString()
    }
}