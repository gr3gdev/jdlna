package org.dev.gr3g.jdlna.utils

import org.dev.gr3g.jdlna.bean.Upnp
import java.net.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object ServerUtils {

    /** Logger.  */
    private val logger = Logger
            .getLogger(ServerUtils::class.java.name)

    @Throws(SocketException::class, UnknownHostException::class)
    private fun findAdress(): InetAddress {
        val enumInterf = NetworkInterface
                .getNetworkInterfaces()
        while (enumInterf.hasMoreElements()) {
            val interf = enumInterf.nextElement()
            val addresses = interf
                    .inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (address is Inet4Address
                        && !address.isLoopbackAddress()
                        && address.isSiteLocalAddress()) {
                    return address
                }
            }
        }
        return InetAddress.getLocalHost()
    }

    val baseURL: String
        get() {
            val build = StringBuilder("http://")
            try {
                val hostName = findAdress().hostAddress
                build.append(hostName)
            } catch (exc: SocketException) {
                logger.log(Level.SEVERE, "Erreur HOSTNAME", exc)
            } catch (exc: UnknownHostException) {
                logger.log(Level.SEVERE, "Erreur HOSTNAME", exc)
            }
            build.append(":").append(System.getenv("PORT") ?: "9300").append(Upnp.NAMESPACE)
            return build.toString()
        }
}