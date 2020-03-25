package org.dev.gr3g.jdlna

import org.dev.gr3g.jdlna.dao.DatabaseDAO
import org.dev.gr3g.jdlna.utils.FilesUtils
import org.dev.gr3g.jdlna.worker.MediaLoader
import org.dev.gr3g.jdlna.worker.UpnpServer
import java.io.IOException
import java.util.logging.LogManager
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
            logManager.readConfiguration(
                    App::class.java.getResourceAsStream("/jdlna-logging.properties"))
        } catch (exc: SecurityException) {
            exitProcess(1)
        } catch (exc: IOException) {
            exitProcess(1)
        }
    }
}