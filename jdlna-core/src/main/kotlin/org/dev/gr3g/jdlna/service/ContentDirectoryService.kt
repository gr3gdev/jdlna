package org.dev.gr3g.jdlna.service

import org.dev.gr3g.jdlna.bean.FileResult
import org.dev.gr3g.jdlna.dao.DatabaseDAO
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.model.container.StorageFolder
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
class ContentDirectoryService : AbstractContentDirectoryService() {

    @Throws(ContentDirectoryException::class)
    override fun browse(objectID: String,
                        browseFlag: BrowseFlag, filter: String?,
                        firstResult: Long, pMaxResults: Long,
                        orderby: Array<SortCriterion?>?): BrowseResult {
        var maxResults = pMaxResults
        return try {
            val result: BrowseResult
            if (maxResults == 0L) {
                maxResults = Int.MAX_VALUE.toLong()
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(java.lang.String.format(
                        "BROWSE: id=%s, flag=%s, filter=%s, first=%d, max=%d",
                        objectID, browseFlag.name, filter, firstResult,
                        maxResults))
            }
            if (browseFlag === BrowseFlag.METADATA) {
                val didl = DIDLContent()
                val root = StorageFolder()
                root.id = "0"
                root.parentID = "-1"
                root.title = "JDLNA"
                root.isRestricted = true
                root.isSearchable = false
                root.writeStatus = WriteStatus.NOT_WRITABLE
                didl.addContainer(root)
                result = BrowseResult(DIDLParser().generate(didl),
                        didl.count, didl.count)
            } else {
                val resSearch: FileResult = DatabaseDAO.select(objectID.toInt().toLong(), firstResult.toInt(),
                        maxResults.toInt())
                val totalMatch: Long = resSearch.totalMatch.toLong()
                val didl: DIDLContent = resSearch.content
                result = BrowseResult(DIDLParser().generate(didl),
                        didl.count, totalMatch)
            }
            result
        } catch (exc: Exception) {
            LOG.log(Level.SEVERE, "Error browse", exc)
            throw ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS, exc.toString())
        }
    }

    @Throws(ContentDirectoryException::class)
    override fun search(containerId: String?,
                        searchCriteria: String?, filter: String?,
                        firstResult: Long, maxResults: Long,
                        orderBy: Array<SortCriterion?>?): BrowseResult {
        // You can override this method to implement searching!
        return super.search(containerId, searchCriteria, filter, firstResult,
                maxResults, orderBy)
    }

    companion object {
        private val LOG = Logger
                .getLogger(ContentDirectoryService::class.java.name)
    }
}