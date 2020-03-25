package org.dev.gr3g.jdlna.web.servlet

import org.fourthline.cling.model.message.Connection
import java.net.InetAddress
import java.net.UnknownHostException
import javax.servlet.http.HttpServletRequest

/**
 * @author Gregory Tardivel
 */
class AsyncServletConnection(var request: HttpServletRequest) : Connection {

    override fun isOpen(): Boolean {
        return true
    }

    override fun getRemoteAddress(): InetAddress {
        return try {
            InetAddress.getByName(request.remoteAddr)
        } catch (ex: UnknownHostException) {
            throw RuntimeException(ex)
        }
    }

    override fun getLocalAddress(): InetAddress {
        return try {
            InetAddress.getByName(request.localAddr)
        } catch (ex: UnknownHostException) {
            throw RuntimeException(ex)
        }
    }

}