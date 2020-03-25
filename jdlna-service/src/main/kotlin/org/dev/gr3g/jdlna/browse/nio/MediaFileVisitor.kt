package org.dev.gr3g.jdlna.browse.nio

import org.dev.gr3g.jdlna.dao.DatabaseDAO
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
class MediaFileVisitor(private val parentPath: Path) : FileVisitor<Path> {

    private fun treatFileOrDirectory(pPath: Path,
                                     pAttrs: BasicFileAttributes) {
        DatabaseDAO.insert(pPath, pAttrs)
    }

    @Throws(IOException::class)
    override fun preVisitDirectory(pDir: Path,
                                   pAttrs: BasicFileAttributes): FileVisitResult {
        if (pDir != parentPath) {
            treatFileOrDirectory(pDir, pAttrs)
            logger.fine("preVisitDirectory : $pDir")
        }
        return FileVisitResult.CONTINUE
    }

    @Throws(IOException::class)
    override fun visitFile(pFile: Path,
                           pAttrs: BasicFileAttributes): FileVisitResult {
        treatFileOrDirectory(pFile, pAttrs)
        logger.fine("visitFile : $pFile")
        return FileVisitResult.CONTINUE
    }

    @Throws(IOException::class)
    override fun visitFileFailed(pFile: Path,
                                 pExc: IOException): FileVisitResult {
        logger.warning("Visit File failed : $pFile")
        return FileVisitResult.CONTINUE
    }

    @Throws(IOException::class)
    override fun postVisitDirectory(pDir: Path,
                                    pExc: IOException): FileVisitResult {
        return FileVisitResult.CONTINUE
    }

    companion object {
        /** Logger.  */
        private val logger = Logger
                .getLogger(MediaFileVisitor::class.java.name)
    }

}