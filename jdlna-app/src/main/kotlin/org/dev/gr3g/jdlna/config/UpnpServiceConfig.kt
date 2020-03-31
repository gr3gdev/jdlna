package org.dev.gr3g.jdlna.config

import org.dev.gr3g.jdlna.bean.Upnp
import org.dev.gr3g.jdlna.web.transport.AsyncServletStreamServerImpl
import org.fourthline.cling.DefaultUpnpServiceConfiguration
import org.fourthline.cling.model.Namespace
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl
import org.fourthline.cling.transport.impl.jetty.StreamClientImpl
import org.fourthline.cling.transport.spi.NetworkAddressFactory
import org.fourthline.cling.transport.spi.StreamClient
import org.fourthline.cling.transport.spi.StreamServer
import java.util.concurrent.ExecutorService
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
class UpnpServiceConfig(pStreamListenPort: Int) : DefaultUpnpServiceConfiguration(pStreamListenPort) {

    override fun createNamespace(): Namespace {
        return Namespace(Upnp.NAMESPACE)
    }

    override fun createStreamClient(): StreamClient<*> {
        val executorService: ExecutorService = this
                .syncProtocolExecutorService
        val config = StreamClientConfigurationImpl(executorService)
        logger.info("Create stream client")
        return StreamClientImpl(config)
    }

    override fun createStreamServer(networkAddressFactory: NetworkAddressFactory): StreamServer<*> {
        val listenPort: Int = networkAddressFactory.streamListenPort
        val config = AsyncServletStreamServerConfigurationImpl(JettyServletContainer.INSTANCE, listenPort)
        logger.info("Create stream server")
        return AsyncServletStreamServerImpl(config)
    }

    companion object {
        private val logger = Logger
                .getLogger(UpnpServiceConfig::class.java.name)
    }
}