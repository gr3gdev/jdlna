package org.dev.gr3g.jdlna.utils

import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.dlna.DLNAProtocolInfo
import org.seamless.util.MimeType

/**
 * @author Gregory Tardivel
 */
object DataUtils {

    private fun getURL(id: String): String {
        val builder = StringBuilder(
                ServerUtils.baseURL)
        builder.append("/file?stream=").append(id)
        return builder.toString()
    }

    fun createResource(id: String, mimeType: String,
                       size: Long): Res {
        val httpGetMimeType = MimeType.valueOf(mimeType)
        val url = getURL(id)
        val protocol = DLNAProtocolInfo(httpGetMimeType)
        return Res(protocol, size, url)
    }
}