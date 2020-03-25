package org.dev.gr3g.jdlna.web.servlet

import org.fourthline.cling.transport.Router
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl
import org.fourthline.cling.transport.impl.AsyncServletUpnpStream
import java.io.IOException
import java.util.logging.Logger
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** @author Gregory Tardivel
 */
class AsyncServlet(pRouter: Router,
                   pConfiguration: AsyncServletStreamServerConfigurationImpl) : HttpServlet() {

    private var mCounter = 0
    private val router: Router = pRouter
    private val configuration: AsyncServletStreamServerConfigurationImpl = pConfiguration

    @Throws(ServletException::class, IOException::class)
    override fun service(req: HttpServletRequest,
                         resp: HttpServletResponse) {
        val startTime = System.currentTimeMillis()
        val counter = mCounter++
        logger.fine(String.format("HttpServlet.service(): id: %3d, request URI: %s",
                counter, req.requestURI))
        val async = req.startAsync()
        async.timeout = (configuration.asyncTimeoutSeconds * 1000).toLong()
        async.addListener(AsyncJdlnaListener(startTime, counter))
        val stream: AsyncServletUpnpStream = AsyncServletUpnpStreamImpl(
                router.protocolFactory, async, req)
        router.received(stream)
    }

    companion object {
        /** Logger.  */
        private val logger = Logger
                .getLogger(AsyncServlet::class.java.name)
    }

}