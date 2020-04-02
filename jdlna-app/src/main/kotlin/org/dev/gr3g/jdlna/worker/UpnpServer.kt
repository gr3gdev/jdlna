package org.dev.gr3g.jdlna.worker

import org.dev.gr3g.jdlna.config.UpnpServiceConfig
import org.dev.gr3g.jdlna.utils.ServerUtils
import org.dev.gr3g.jdlna.utils.UpnpUtils
import org.fourthline.cling.UpnpService
import org.fourthline.cling.UpnpServiceImpl
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

/**
 * @author Gregory Tardivel
 */
class UpnpServer : Runnable {

    override fun run() {
        try {
            val upnpService: UpnpService = UpnpServiceImpl(
                    UpnpServiceConfig((System.getenv("PORT") ?: "9300").toInt()))

            // Asynch search for other devices (most importantly UPnP-enabled
            // routers for port-mapping)
            upnpService.controlPoint.search()
            Runtime.getRuntime().addShutdownHook(object : Thread() {
                override fun run() {
                    upnpService.shutdown()
                }
            })

            // Add the bound local device to the registry
            upnpService.registry.addDevice(UpnpUtils.createDevice())
            logger.info("Server started : " + ServerUtils.baseURL)
        } catch (exc: Exception) {
            logger.log(Level.SEVERE, "Erreur serveur", exc)
            exitProcess(1)
        }
    }

    companion object {
        /** Logger. */
        private val logger = Logger
                .getLogger(UpnpServer::class.java.name)
    }
}