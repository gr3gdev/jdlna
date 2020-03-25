package org.dev.gr3g.jdlna.web.transport

import org.dev.gr3g.jdlna.web.servlet.AsyncServlet
import org.fourthline.cling.transport.Router
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl
import org.fourthline.cling.transport.spi.InitializationException
import org.fourthline.cling.transport.spi.StreamServer
import java.net.InetAddress
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
class AsyncServletStreamServerImpl(
        private val configuration: AsyncServletStreamServerConfigurationImpl) : StreamServer<AsyncServletStreamServerConfigurationImpl> {

    private var port = 0
    private var hostAddress: String? = null

    override fun run() {
        getConfiguration().servletContainerAdapter
                .startIfNotRunning()
    }

    @Throws(InitializationException::class)
    override fun init(bindAddress: InetAddress, router: Router) {
        try {
            LOGGER.fine(
                    "Setting executor service on servlet container adapter")
            getConfiguration().servletContainerAdapter
                    .setExecutorService(router.configuration
                            .streamServerExecutorService)
            hostAddress = bindAddress.hostAddress
            LOGGER.fine(String.format("Adding connector: %s:%d",
                    hostAddress, getConfiguration().listenPort))
            port = getConfiguration().servletContainerAdapter
                    .addConnector(hostAddress,
                            getConfiguration().listenPort)
            val contextPath = router.configuration.namespace
                    .basePath.path
            getConfiguration().servletContainerAdapter
                    .registerServlet(contextPath,
                            AsyncServlet(router, configuration))
        } catch (ex: Exception) {
            throw InitializationException("Could not initialize "
                    + this.javaClass.simpleName + ": " + ex.toString(),
                    ex)
        }
    }

    override fun getPort(): Int {
        return port
    }

    override fun stop() {
        getConfiguration().servletContainerAdapter
                .removeConnector(hostAddress, port)
    }

    override fun getConfiguration(): AsyncServletStreamServerConfigurationImpl {
        return configuration
    }

    companion object {
        /** Logger.  */
        private val LOGGER = Logger
                .getLogger(AsyncServletStreamServerImpl::class.java.name)
    }

}