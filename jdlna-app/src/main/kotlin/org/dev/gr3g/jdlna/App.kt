package org.dev.gr3g.jdlna

import org.dev.gr3g.jdlna.dao.DatabaseDAO
import org.dev.gr3g.jdlna.logging.format.LogFormatter
import org.dev.gr3g.jdlna.utils.FilesUtils
import org.dev.gr3g.jdlna.worker.MediaLoader
import org.dev.gr3g.jdlna.worker.UpnpServer
import java.io.IOException
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.system.exitProcess

/** @author Gregory Tardivel
 */
object App {

    private val logManager = LogManager.getLogManager()

    /** @param args
     * @throws IOException
     * @throws SecurityException
     */
    @Throws(SecurityException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        FilesUtils.init()
        DatabaseDAO.init()

        // Chargement des fichiers
        val mediaThread = Thread(MediaLoader())
        mediaThread.isDaemon = false
        mediaThread.start()

        // Serveur Media UPNP
        val serverThread = Thread(UpnpServer())
        serverThread.isDaemon = false
        serverThread.start()
    }

    init {
        try {
            val globalLogger = Logger.getLogger("global")
            globalLogger.level = Level.INFO
            val pattern = (System.getenv("LOG_DIR") ?: "/var/log/jdlna") + "/jdlna%u.log"
            val fileHandler = FileHandler(pattern, 500000, 5)
            fileHandler.formatter = LogFormatter()
            fileHandler.level = Level.INFO
            globalLogger.addHandler(fileHandler)
            logManager.loggerNames.toList().forEach {
                logManager.getLogger(it).addHandler(fileHandler)
            }
        } catch (exc: SecurityException) {
            exitProcess(1)
        } catch (exc: IOException) {
            exitProcess(1)
        }
    }
}