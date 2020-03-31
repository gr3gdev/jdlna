package org.dev.gr3g.jdlna.utils

import org.dev.gr3g.jdlna.browse.nio.MediaFileVisitor
import org.dev.gr3g.jdlna.dao.DatabaseDAO
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object MediaFileUtils {

    /** Logger.  */
    private val logger = Logger
            .getLogger(MediaFileUtils::class.java.name)

    @Throws(IOException::class)
    fun update() {
        val folderVideos = DatabaseDAO.selectConf("folder.video")
        val folderMusics = DatabaseDAO.selectConf("folder.music")
        val folderPhotos = DatabaseDAO.selectConf("folder.photo")
        init(Paths.get(folderVideos), Paths.get(folderPhotos),
                Paths.get(folderMusics))
    }

    @Throws(IOException::class)
    private fun parse(pPath: Path?) {
        if (pPath != null && Files.exists(pPath)) {
            logger.info("Loading : $pPath")
            val visitor = MediaFileVisitor(pPath)
            Files.walkFileTree(pPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Int.MAX_VALUE, visitor)
        }
    }

    @Throws(IOException::class)
    fun init(videoPath: Path?, imagePath: Path?,
             musicPath: Path?) {
        logger.info("Initalisation...")
        DatabaseDAO.clean()
        val idRoot = DatabaseDAO.insert(null, "ROOT", -1)
        if (videoPath != null) {
            DatabaseDAO.insert(videoPath, "Vidéos", idRoot)
            parse(videoPath)
        }
        if (musicPath != null) {
            DatabaseDAO.insert(musicPath, "Musics", idRoot)
            parse(musicPath)
        }
        if (imagePath != null) {
            DatabaseDAO.insert(imagePath, "Images", idRoot)
            parse(imagePath)
        }
    }
}