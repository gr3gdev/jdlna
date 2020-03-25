package org.dev.gr3g.jdlna.web.servlet

import java.io.IOException
import java.util.logging.Logger
import javax.servlet.AsyncEvent
import javax.servlet.AsyncListener

/**
 * @author Gregory Tardivel
 */
class AsyncJdlnaListener(private val startTime: Long, private val counter: Int) : AsyncListener {

    @Throws(IOException::class)
    override fun onTimeout(arg0: AsyncEvent) {
        val duration = System.currentTimeMillis() - startTime
        logger.fine(String.format(
                "AsyncListener.onTimeout(): id: %3d, duration: %,4d",
                counter, duration))
    }

    @Throws(IOException::class)
    override fun onStartAsync(arg0: AsyncEvent) {
        logger.fine(String.format("AsyncListener.onStartAsync(): id: %3d",
                counter))
    }

    @Throws(IOException::class)
    override fun onError(arg0: AsyncEvent) {
        val duration = System.currentTimeMillis() - startTime
        logger.fine(String.format(
                "AsyncListener.onlog(Level.SEVERE, ): id: %3d, duration: %,4d",
                counter, duration))
    }

    @Throws(IOException::class)
    override fun onComplete(arg0: AsyncEvent) {
        val duration = System.currentTimeMillis() - startTime
        logger.fine(String.format(
                "AsyncListener.onComplete(): id: %3d, duration: %,4d",
                counter, duration))
    }

    companion object {
        /** Logger. */
        private val logger = Logger
                .getLogger(AsyncJdlnaListener::class.java.name)
    }

}