package org.dev.gr3g.jdlna.dao

import org.dev.gr3g.jdlna.bean.ConfigModel
import org.dev.gr3g.jdlna.bean.FileResult
import org.dev.gr3g.jdlna.bean.MediaType
import org.dev.gr3g.jdlna.bean.container.JdlnaContainer
import org.dev.gr3g.jdlna.utils.DataUtils
import org.dev.gr3g.jdlna.utils.FilesUtils.generateID
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.item.MusicTrack
import org.fourthline.cling.support.model.item.Photo
import org.fourthline.cling.support.model.item.VideoBroadcast
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object DatabaseDAO {

    /** Logger.  */
    private val logger = Logger.getLogger(DatabaseDAO::class.java.name)

    private val MATCHER_VIDEO = FileSystems.getDefault()
            .getPathMatcher(
                    "regex:(?i).*\\.(avi|mpg|mpeg|mp4|mkv|wmv|mov|asf|3gp)")
    private val MATCHER_MUSIC = FileSystems.getDefault()
            .getPathMatcher("regex:(?i).*\\.(mp3|flac)")
    private val MATCHER_IMAGE = FileSystems.getDefault()
            .getPathMatcher("regex:(?i).*\\.(bmp|jpg|jpeg|gif|png)")

    private const val PATH_NONE = "none"
    private const val MIME_TYPE_FOLDER = "folder"

    private val requests = PropertyResourceBundle(InputStreamReader(ConnectionUtils::class.java.getResourceAsStream("/requetes.properties"), StandardCharsets.UTF_8))

    private val CREATE_TABLE_FILE = requests.getString("db.create.table.file")
    private val CREATE_TABLE_CONF = requests.getString("db.create.table.conf")
    private val CREATE_INDEX_FILE = requests.getString("db.create.index.file")
    private val CREATE_INDEX_FILE_PARENT = requests.getString("db.create.index.file_parent")
    private val CREATE_INDEX_FILE_TYPE = requests.getString("db.create.index.file_type")

    private val COUNT_BY_PARENT = requests.getString("db.file.countByParent")
    private val COUNT_BY_TYPE = requests.getString("db.file.countByType")
    private val DELETE = requests.getString("db.file.delete")
    private val INSERT = requests.getString("db.file.insert")
    private val INSERT_CONF = requests.getString("db.conf.insert")
    private val SELECT = requests.getString("db.conf.select")
    private val SELECT_PATH = requests.getString("db.file.selectPath")
    private val SELECT_PATHS = requests.getString("db.file.selectPaths")
    private val SELECT_BY_ID = requests.getString("db.file.selectById")
    private val SELECT_BY_NAME = requests.getString("db.conf.selectByName")
    private val SELECT_BY_PARENT = requests.getString("db.file.selectByParent")
    private val UPDATE = requests.getString("db.conf.update")

    const val FOLDER_VIDEO = "folder.video"
    const val FOLDER_MUSIC = "folder.music"
    const val FOLDER_IMAGE = "folder.image"
    const val FOLDER_COVER = "folder.cover"

    fun init() {
        ConnectionUtils.openConnection().use { cnx ->
            cnx.createStatement().use {
                it.execute(CREATE_TABLE_FILE)
            }
            cnx.createStatement().use {
                it.execute(CREATE_TABLE_CONF)
            }
            cnx.createStatement().use {
                it.execute(CREATE_INDEX_FILE)
            }
            cnx.createStatement().use {
                it.execute(CREATE_INDEX_FILE_PARENT)
            }
            cnx.createStatement().use {
                it.execute(CREATE_INDEX_FILE_TYPE)
            }
            val conf = selectConf()
            if (conf.video.path == null) {
                insertConf(cnx, FOLDER_VIDEO, System.getenv("FOLDER_VIDEO") ?: "/media/videos")
            }
            if (conf.music.path == null) {
                insertConf(cnx, FOLDER_MUSIC, System.getenv("FOLDER_MUSIC") ?: "/media/musics")
            }
            if (conf.photo.path == null) {
                insertConf(cnx, FOLDER_IMAGE, System.getenv("FOLDER_IMAGE") ?: "/media/images")
            }
            if (conf.cover.path == null) {
                insertConf(cnx, FOLDER_COVER, System.getenv("FOLDER_COVER") ?: "/media/covers")
            }
        }
    }

    private fun fileExists(cnx: Connection, id: Int): Boolean {
        cnx.prepareStatement(SELECT_BY_ID).use {
            it.setInt(1, id)
            it.executeQuery().use { res ->
                return res.next()
            }
        }
    }

    fun insert(pPath: Path?, name: String?, parent: Int): Int {
        var id = 0
        ConnectionUtils.openConnection().use { cnx ->
            val size: Long = 0
            var strPath = PATH_NONE
            if (pPath != null) {
                strPath = pPath.toString()
                id = generateID(strPath, size)
            }
            val mimeType = MIME_TYPE_FOLDER
            val type = MediaType.DIRECTORY
            if (!fileExists(cnx, id)) {
                cnx.prepareStatement(INSERT).use {
                    it.setInt(1, id)
                    it.setInt(2, parent)
                    it.setInt(3, 0)
                    it.setString(4, name)
                    it.setString(5, mimeType)
                    it.setLong(6, size)
                    it.setString(7, strPath)
                    it.setInt(8, type.index)
                    it.executeUpdate()
                }
            } else {
                // TODO Purge or update
            }
        }
        return id
    }

    fun insert(pPath: Path,
               pAttrs: BasicFileAttributes): Int {
        var id = 0
        val conf = selectConf("folder.cover")
        val coverPath = Paths.get(conf)
        ConnectionUtils.openConnection().use { cnx ->
            try {
                val name = pPath.fileName.toString()
                var size = pAttrs.size()
                if (pAttrs.isDirectory) {
                    size = 0
                }
                val strPath = pPath.toString()
                id = generateID(strPath, size)
                var parent = 0
                if (pPath.parent != null) {
                    parent = generateID(pPath.parent.toString(), 0)
                }
                var idCover = 0
                var mimeType = Files.probeContentType(pPath)
                if (mimeType == null && pAttrs.isDirectory) {
                    mimeType = MIME_TYPE_FOLDER
                } else if (pPath.fileName.toString().toLowerCase()
                                .endsWith(".mkv")) {
                    mimeType = "video/x-matroska"
                }
                val type: MediaType
                if (pAttrs.isDirectory) {
                    type = MediaType.DIRECTORY
                } else if (MATCHER_VIDEO.matches(pPath)) {
                    type = MediaType.VIDEO
                    val idxPoint = name.lastIndexOf(".")
                    val fileName = name.substring(0, idxPoint)
                    val cover = StringBuilder(coverPath.toString())
                            .append("/").append(fileName).append(".jpg")
                            .toString()
                    val cPath = Paths.get(cover)
                    if (Files.exists(cPath)) {
                        idCover = insertCover(cnx, cPath)
                    }
                } else if (MATCHER_MUSIC.matches(pPath)) {
                    type = MediaType.MUSIC
                } else if (MATCHER_IMAGE.matches(pPath)) {
                    type = MediaType.IMAGE
                } else {
                    type = MediaType.UNDEFINED
                    logger.warning("Undefined type : $pPath")
                }
                if (!fileExists(cnx, id)) {
                    cnx.prepareStatement(INSERT).use {
                        it.setInt(1, id)
                        it.setInt(2, parent)
                        it.setInt(3, idCover)
                        it.setString(4, name)
                        it.setString(5, mimeType)
                        it.setLong(6, size)
                        it.setString(7, strPath)
                        it.setInt(8, type.index)
                        it.executeUpdate()
                    }
                } else {
                    // TODO Purge or update
                }
            } catch (exc: IOException) {
                logger.log(Level.SEVERE, "Error INSERT", exc)
            }
        }
        return id
    }

    @Throws(IOException::class)
    private fun insertCover(cnx: Connection, pCPath: Path): Int {
        val size = Files.size(pCPath)
        val name = pCPath.fileName.toString()
        val id = generateID(pCPath.toString(), size)
        val parent = -1
        val idCover = 0
        val mimeType = Files.probeContentType(pCPath)
        if (!fileExists(cnx, id)) {
            cnx.prepareStatement(INSERT).use {
                it.setInt(1, id)
                it.setInt(2, parent)
                it.setInt(3, idCover)
                it.setString(4, name)
                it.setString(5, mimeType)
                it.setLong(6, size)
                it.setString(7, pCPath.toString())
                it.setInt(8, MediaType.COVER.index)
                it.executeUpdate()
            }
        } else {
            // TODO Purge or update
        }
        return id
    }

    fun selectPath(id: Long): Path {
        var path = ""
        ConnectionUtils.openConnection().use { cnx ->
            cnx.prepareStatement(SELECT_PATH).use {
                it.setLong(1, id)
                it.executeQuery().use { res ->
                    if (res.next()) {
                        path = res.getString(1)
                    }
                }
            }
        }
        return Paths.get(path)
    }

    fun select(idParent: Long, first: Int,
               max: Int): FileResult {
        val result = FileResult()
        val content = DIDLContent()
        ConnectionUtils.openConnection().use { cnx ->
            cnx.prepareStatement(COUNT_BY_PARENT).use {
                it.setLong(1, idParent)
                it.executeQuery().use { res ->
                    if (res.next()) {
                        result.totalMatch = res.getInt(1)
                    }
                }
            }
            cnx.prepareStatement(SELECT_BY_PARENT).use {
                it.setLong(1, idParent)
                it.setInt(2, max)
                it.setInt(3, first)
                it.executeQuery().use { res ->
                    while (res.next()) {
                        val type = MediaType
                                .valueOfIndex(res.getInt("TYPE"))
                        val id = res.getString("ID")
                        val name = res.getString("NAME")
                        val parent = res.getString("ID_PARENT")
                        val idCover = res.getString("ID_COVER")
                        val strPath = res.getString("PATH")
                        val mimeType = res.getString("MIME_TYPE")
                        val size = res.getLong("SIZE")
                        var resource: Res? = null
                        if (strPath != null
                                && !PATH_NONE.equals(strPath, ignoreCase = true)) {
                            if (mimeType != null && !MIME_TYPE_FOLDER
                                            .equals(mimeType, ignoreCase = true)) {
                                resource = DataUtils.createResource(id, mimeType, size)
                            }
                        }
                        when (type) {
                            MediaType.DIRECTORY -> {
                                val container = JdlnaContainer(id,
                                        parent, name)
                                content.addContainer(container)
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine(String.format("Container: %s", name))
                                }
                            }
                            MediaType.VIDEO -> {
                                var videoItem: VideoBroadcast
                                var resCover: Res? = null
                                if ("0" != idCover) {
                                    resCover = selectCover(cnx, idCover)
                                }
                                if (resCover != null) {
                                    videoItem = VideoBroadcast(id, parent, name,
                                            "INCONNU", resource, resCover)
                                    videoItem.icon = URI(resCover.value)
                                } else {
                                    videoItem = VideoBroadcast(id, parent, name,
                                            "INCONNU", resource)
                                }
                                content.addItem(videoItem)
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine(String.format("VideoBroadcast: %s", name))
                                }
                            }
                            MediaType.MUSIC -> {
                                val musicItem = MusicTrack(id, parent,
                                        name, "INCONNU", "INCONNU", "INCONNU",
                                        resource)
                                content.addItem(musicItem)
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine(String.format("MusicTrack: %s", name))
                                }
                            }
                            MediaType.IMAGE -> {
                                val imageItem = Photo(id, parent, name,
                                        "INCONNU", "INCONNU", resource)
                                content.addItem(imageItem)
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine(String.format("Photo: %s", name))
                                }
                            }
                            else -> logger.warning("Type undefined: $type")
                        }
                    }
                }
            }
        }
        result.content = content
        return result
    }

    @Throws(SQLException::class)
    private fun selectCover(pCnx: Connection, pIdCover: String): Res? {
        var cover: Res? = null
        pCnx.prepareStatement(SELECT_BY_ID).use {
            it.setString(1, pIdCover)
            it.executeQuery().use { res ->
                if (res.next()) {
                    val mimeType = res.getString("MIME_TYPE")
                    val size = res.getLong("SIZE")
                    cover = DataUtils.createResource(pIdCover, mimeType, size)
                }
            }
        }
        return cover
    }

    fun clean() {
        ConnectionUtils.openConnection().use { cnx ->
            cnx.createStatement().use {
                it.executeQuery(SELECT_PATHS).use { res ->
                    while (res.next()) {
                        val id = res.getInt("ID")
                        val strPath = res.getString("PATH")
                        val path = Paths.get(strPath)
                        if (!Files.exists(path)) {
                            logger.fine(String.format("DELETE (%d, %s)", id, strPath))
                            cnx.prepareStatement(DELETE).use { del ->
                                del.setInt(1, id)
                                del.executeUpdate()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun insertConf(cnx: Connection, name: String, value: String?) {
        val id = generateID(name, 8L)
        cnx.prepareStatement(INSERT_CONF).use {
            it.setInt(1, id)
            it.setString(2, name)
            it.setString(3, value)
            it.executeUpdate()
        }
    }

    fun updateConf(name: String?, value: String?) {
        ConnectionUtils.openConnection().use { cnx ->
            cnx.prepareStatement(UPDATE).use {
                it.setString(2, name)
                it.setString(1, value)
                it.executeUpdate()
            }
        }
    }

    fun selectConf(): ConfigModel {
        val conf = ConfigModel()
        ConnectionUtils.openConnection().use { cnx ->
            cnx.createStatement().use {
                it.executeQuery(SELECT).use { res ->
                    while (res.next()) {
                        when (res.getString(1)) {
                            FOLDER_VIDEO -> conf.video.path = res.getString(2)
                            FOLDER_MUSIC -> conf.music.path = res.getString(2)
                            FOLDER_IMAGE -> conf.photo.path = res.getString(2)
                            FOLDER_COVER -> conf.cover.path = res.getString(2)
                        }
                    }
                }
            }
            arrayOf(MediaType.VIDEO, MediaType.COVER, MediaType.IMAGE, MediaType.MUSIC).forEach { type ->
                cnx.prepareStatement(COUNT_BY_TYPE).use {
                    it.setInt(1, type.index)
                    it.executeQuery().use { res ->
                        if (res.next()) {
                            when (type) {
                                MediaType.MUSIC -> conf.music.count = res.getInt(1)
                                MediaType.VIDEO -> conf.video.count = res.getInt(1)
                                MediaType.IMAGE -> conf.photo.count = res.getInt(1)
                                MediaType.COVER -> conf.cover.count = res.getInt(1)
                                MediaType.DIRECTORY -> logger.warning("Directory not configured")
                                MediaType.UNDEFINED -> logger.warning("Undefined type")
                            }
                        }
                    }
                }
            }
        }
        return conf
    }

    fun selectConf(name: String?): String {
        var value = "error"
        ConnectionUtils.openConnection().use { cnx ->
            cnx.prepareStatement(SELECT_BY_NAME).use {
                it.setString(1, name)
                it.executeQuery().use { res ->
                    if (res.next()) {
                        value = res.getString(1)
                    }
                }
            }
        }
        return value
    }

}