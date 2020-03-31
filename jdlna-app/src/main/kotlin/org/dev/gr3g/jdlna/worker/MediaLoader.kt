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
        val confVideoPath = DatabaseDAO.selectConf("folder.video")
        var videoPath: Path? = null
        if (confVideoPath.isNotEmpty()) {
            Paths.get(confVideoPath)
        }
        val confPhotoPath = DatabaseDAO.selectConf("folder.photo")
        val imagePath: Path? = null
        if (confPhotoPath.isNotEmpty()) {
            Paths.get(confPhotoPath)
        }
        val confMusicPath = DatabaseDAO.selectConf("folder.music")
        val musicPath: Path? = null
        if (confMusicPath.isNotEmpty()) {
            Paths.get(confMusicPath)
        }
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