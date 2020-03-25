package org.dev.gr3g.jdlna.worker

import org.dev.gr3g.jdlna.dao.DatabaseDAO
import org.dev.gr3g.jdlna.utils.MediaFileUtils
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
class MediaLoader : Runnable {

    override fun run() {
        val videoPath: Path = Paths
                .get(DatabaseDAO.selectConf("folder.video"))
        val imagePath: Path = Paths
                .get(DatabaseDAO.selectConf("folder.photo"))
        val musicPath: Path = Paths
                .get(DatabaseDAO.selectConf("folder.music"))
        try {
            MediaFileUtils.init(videoPath, imagePath, musicPath)
        } catch (exc: IOException) {
            logger.log(Level.SEVERE, "Error loading media files", exc)
        }
    }

    companion object {
        /** Logger. */
        private val logger = Logger.getLogger(MediaLoader::class.java.name)
    }
}