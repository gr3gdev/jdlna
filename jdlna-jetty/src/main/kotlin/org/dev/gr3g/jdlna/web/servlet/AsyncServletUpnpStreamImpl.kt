package org.dev.gr3g.jdlna.web.servlet

import org.dev.gr3g.jdlna.bean.MediaType
import org.dev.gr3g.jdlna.bean.Upnp
import org.dev.gr3g.jdlna.dao.DatabaseDAO
import org.dev.gr3g.jdlna.utils.FilesUtils
import org.dev.gr3g.jdlna.utils.MediaFileUtils
import org.fourthline.cling.model.message.Connection
import org.fourthline.cling.model.message.StreamRequestMessage
import org.fourthline.cling.model.message.StreamResponseMessage
import org.fourthline.cling.protocol.ProtocolFactory
import org.fourthline.cling.transport.impl.AsyncServletUpnpStream
import org.seamless.util.MimeType
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger
import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Gregory Tardivel
 */
@Suppress("NAME_SHADOWING")
class AsyncServletUpnpStreamImpl(protocolFactory: ProtocolFactory?,
                                 asyncContext: AsyncContext?, request: HttpServletRequest?) : AsyncServletUpnpStream(protocolFactory, asyncContext, request) {

    private var message: String? = ""
    override fun createConnection(): Connection {
        return AsyncServletConnection(getRequest())
    }

    override fun run() {
        try {
            val requestMessage = readRequestMessage()
            LOGGER.fine(String.format("Processing new request message: %s",
                    requestMessage.toString()))
            val path = getRequest().pathInfo
            LOGGER.fine(String.format("process() PATH: %s", path))
            when {
                path == Upnp.NAMESPACE + "/" -> {
                    processIndex()
                }
                path.startsWith(Upnp.NAMESPACE + "/js") -> {
                    processStatic(path, "text/javascript")
                }
                path.startsWith(Upnp.NAMESPACE + "/css") -> {
                    processStatic(path, "text/css")
                }
                path.startsWith(Upnp.NAMESPACE + "/file") -> {
                    processFileStreaming()
                }
                path.startsWith(Upnp.NAMESPACE + "/reload") -> {
                    processReload()
                }
                path.startsWith(Upnp.NAMESPACE + "/save") -> {
                    processSave()
                }
                else -> {
                    processDefault(requestMessage)
                }
            }
            message = ""
        } catch (exc: IOException) {
            LOGGER.warning("Exception occurred during UPnP stream processing : "
                    + exc.message)
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.SEVERE, "Error", exc)
            }
            if (!this.response.isCommitted) {
                LOGGER.warning(
                        "Response hasn't been committed, returning INTERNAL SERVER ERROR to client")
                this.response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            } else {
                LOGGER.info(
                        "Could not return INTERNAL SERVER ERROR to client, response was already committed")
            }
            message = exc.message
            responseException(exc)
        } finally {
            complete()
        }
    }

    @Throws(IOException::class)
    private fun sendResponse() {
        if (responseMessage != null) {
            writeResponseMessage(responseMessage)
        } else {
            this.response.status = HttpServletResponse.SC_NOT_FOUND
        }
    }

    @Throws(IOException::class)
    private fun processDefault(requestMessage: StreamRequestMessage) {
        responseMessage = super.process(requestMessage)
        sendResponse()
    }

    @Throws(IOException::class)
    private fun processSave() {
        val port = getRequest().getParameter("port")
        if (port != null) {
            DatabaseDAO.updateConf("port", port)
        }
        val name = getRequest().getParameter("name")
        if (name != null) {
            DatabaseDAO.updateConf("name", name)
        }
        val folderVideo = getRequest()
                .getParameter("folder.video")
        if (folderVideo != null) {
            DatabaseDAO.updateConf("folder.video", folderVideo)
        }
        val folderPhoto = getRequest()
                .getParameter("folder.photo")
        if (folderPhoto != null) {
            DatabaseDAO.updateConf("folder.photo", folderPhoto)
        }
        val folderMusic = getRequest()
                .getParameter("folder.music")
        if (folderMusic != null) {
            DatabaseDAO.updateConf("folder.music", folderMusic)
        }
        val folderCover = getRequest()
                .getParameter("folder.cover")
        if (folderCover != null) {
            DatabaseDAO.updateConf("folder.cover", folderCover)
        }
        processReload()
    }

    @Throws(IOException::class)
    private fun processReload() {
        message = try {
            MediaFileUtils.update()
            "Rechargement des fichiers terminés"
        } catch (exc: IOException) {
            LOGGER.log(Level.SEVERE, "Error update", exc)
            "Erreur: " + exc.message
        }
        processIndex()
    }

    @Throws(IOException::class)
    private fun processStatic(path: String, mimeType: String) {
        val pathStatic: String = path.substring(Upnp.NAMESPACE.length)
        val content: String = FilesUtils.getFileContent(pathStatic)
        responseMessage = StreamResponseMessage(content,
                MimeType.valueOf(mimeType))
        sendResponse()
    }

    @Throws(IOException::class)
    private fun processIndex() {
        val index: String = FilesUtils.getFileContent("/pages/index.html")
        val html = StringBuilder()
        val nbVideos: Int = DatabaseDAO.countByType(MediaType.VIDEO)
        val nbMusics: Int = DatabaseDAO.countByType(MediaType.MUSIC)
        val nbImages: Int = DatabaseDAO.countByType(MediaType.IMAGE)
        val nbCovers: Int = DatabaseDAO.countByType(MediaType.COVER)
        val folderVideo: String = DatabaseDAO.selectConf("folder.video")
        val folderMusic: String = DatabaseDAO.selectConf("folder.music")
        val folderPhoto: String = DatabaseDAO.selectConf("folder.photo")
        val folderCover: String = DatabaseDAO.selectConf("folder.cover")
        html.append(String.format(index, message, folderVideo, nbVideos,
                folderMusic, nbMusics, folderPhoto, nbImages, folderCover,
                nbCovers))
        responseMessage = StreamResponseMessage(html.toString(),
                MimeType.valueOf("text/html"))
        sendResponse()
    }

    @Throws(IOException::class)
    private fun processFileStreaming() {
        val stream = getRequest().getParameter("stream")
        if (stream!!.matches(Regex("[-0-9]*"))) {
            val path: Path = DatabaseDAO.selectPath(stream.toLong())
            val mimeType = Files.probeContentType(path)
            val length = Files.size(path)
            val input = Files.newInputStream(path)
            this.response.status = HttpServletResponse.SC_PARTIAL_CONTENT
            this.response.addHeader("CONTENT-TYPE", mimeType)
            this.response.addHeader("CONTENT-LENGTH", length.toString())
            this.response.addHeader("Accept-Ranges", "bytes")
            this.response.addHeader("Content-Disposition",
                    "filename=" + path.fileName)
            val out: OutputStream = this.response.outputStream
            input.use {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("Stream du fichier %s", stream))
                }

                // final byte[] bytes = new byte[BUFFER];
                val read = Channels.newChannel(it)
                val write = Channels.newChannel(out)

                // final BufferedOutputStream bOut = new
                // BufferedOutputStream(out);
                // while (input.read(bytes) != -1) {
                // bOut.write(bytes);
                // }
                val buff = ByteBuffer.allocate(BUFFER)
                while (read.read(buff) != -1) {
                    buff.flip()
                    write.write(buff)
                    buff.compact()
                }
            }
        } else {
            LOGGER.warning("ID is not an integer: $stream")
        }
    }

    companion object {
        /** Logger.  */
        private val LOGGER = Logger
                .getLogger(AsyncServletUpnpStreamImpl::class.java.name)
        private const val BUFFER = 1024 * 100
    }
}