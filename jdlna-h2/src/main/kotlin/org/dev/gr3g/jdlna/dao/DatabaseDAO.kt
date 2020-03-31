package org.dev.gr3g.jdlna.dao

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
import java.net.URISyntaxException
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
    private const val MIMETYPE_FOLDER = "folder"

    private val requests = PropertyResourceBundle(InputStreamReader(SQLUtils::class.java.getResourceAsStream("/requetes.properties"), StandardCharsets.UTF_8))

    private val CREATE = requests.getString("db.create")
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

    fun init() {
        SQLUtils.openConnection().use { cnx ->
            SQLUtils.execute(cnx, CREATE)
            insertConf(cnx, "port", "9300")
            insertConf(cnx, "name", "JDLNA")
            insertConf(cnx, "folder.video", System.getenv("FOLDER_VIDEO") ?: "")
            insertConf(cnx, "folder.music", System.getenv("FOLDER_MUSIC") ?: "")
            insertConf(cnx, "folder.photo", System.getenv("FOLDER_PHOTO") ?: "")
            insertConf(cnx, "folder.cover", System.getenv("FOLDER_COVER") ?: "")
        }
    }

    fun insert(pPath: Path?, name: String?,
               parent: Int): Int {
        var id = 0
        SQLUtils.openConnection().use { cnx ->
            val size: Long = 0
            var strPath = PATH_NONE
            if (pPath != null) {
                strPath = pPath.toString()
                id = generateID(strPath, size)
            }
            val mimeType = MIMETYPE_FOLDER
            val type = MediaType.DIRECTORY
            SQLUtils.execute(cnx, INSERT, arrayOf(id, parent, 0, name, mimeType, size,
                    strPath, type.index))
            logger.fine(String.format("Insert (%d, %d, %d, %s, %s, %d, %s, %d)",
                    id, parent, 0, name, mimeType, size, strPath,
                    type.index))
        }
        return id
    }

    fun insert(pPath: Path,
               pAttrs: BasicFileAttributes): Int {
        var id = 0
        val conf = selectConf("folder.cover")
        val coverPath = Paths.get(conf)
        SQLUtils.openConnection().use { cnx ->
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
                    mimeType = MIMETYPE_FOLDER
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
                SQLUtils.execute(cnx, INSERT, arrayOf(id, parent, idCover, name, mimeType,
                        size, strPath, type.index))
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(String.format("Insert (%d, %d, %d, %s, %s, %d, %s, %d)",
                            id, parent, idCover, name, mimeType, size,
                            strPath, type.index))
                }
            } catch (exc: IOException) {
                logger.log(Level.SEVERE, "Error INSERT", exc)
            }
        }
        return id
    }

    @Throws(IOException::class)
    private fun insertCover(pCnx: Connection, pCPath: Path): Int {
        val size = Files.size(pCPath)
        val name = pCPath.fileName.toString()
        val id = generateID(pCPath.toString(), size)
        val parent = -1
        val idCover = 0
        val mimeType = Files.probeContentType(pCPath)
        SQLUtils.execute(pCnx, INSERT, arrayOf(id, parent, idCover, name, mimeType,
                size, pCPath.toString(), MediaType.COVER.index))
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format(
                    "Insert COVER (%d, %d, %d, %s, %s, %d, %s, %d)", id, parent,
                    idCover, name, mimeType, size, pCPath.toString(),
                    MediaType.COVER.index))
        }
        return id
    }

    fun selectPath(id: Long): Path {
        var path = ""
        SQLUtils.openConnection().use { cnx ->
            try {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(String.format("SELECT PATH (%d)", id))
                }
                SQLUtils.execute(cnx, SELECT_PATH, arrayOf(id)).use { res ->
                    if (res != null && res.next()) {
                        path = res.getString(1)
                    }
                }
            } catch (exc: SQLException) {
                logger.log(Level.SEVERE, "Erreur SELECT", exc)
            }
        }
        return Paths.get(path)
    }

    fun select(idParent: Long, first: Int,
               max: Int): FileResult {
        val result = FileResult()
        val content = DIDLContent()
        SQLUtils.openConnection().use { cnx ->
            try {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(String.format("SELECT by PARENT (%d, %d, %d)", idParent, first, max))
                }
                SQLUtils.execute(cnx, COUNT_BY_PARENT, arrayOf(idParent)).use { res1 ->
                    if (res1 != null && res1.next()) {
                        result.totalMatch = res1.getInt(1)
                    }
                }
                SQLUtils.execute(cnx, SELECT_BY_PARENT,
                        arrayOf(idParent, max, first)).use { res2 ->
                    if (res2 != null) {
                        while (res2.next()) {
                            val type = MediaType
                                    .valueOfIndex(res2.getInt("TYPE"))
                            val id = res2.getString("ID")
                            val name = res2.getString("NAME")
                            val parent = res2.getString("ID_PARENT")
                            val idCover = res2.getString("ID_COVER")
                            val strPath = res2.getString("PATH")
                            val mimeType = res2.getString("MIMETYPE")
                            val size = res2.getLong("SIZE")
                            var resource: Res? = null
                            if (strPath != null
                                    && !PATH_NONE.equals(strPath, ignoreCase = true)) {
                                if (mimeType != null && !MIMETYPE_FOLDER
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
                                        videoItem.setIcon(URI(resCover.getValue()))
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
            } catch (exc: SQLException) {
                logger.log(Level.SEVERE, "Error SELECT", exc)
            } catch (exc: URISyntaxException) {
                logger.log(Level.SEVERE, "Error SELECT", exc)
            }
        }
        result.content = content
        return result
    }

    @Throws(SQLException::class)
    private fun selectCover(pCnx: Connection, pIdCover: String): Res? {
        var cover: Res? = null
        SQLUtils.execute(pCnx, SELECT_BY_ID, arrayOf(pIdCover)).use { res ->
            if (res != null && res.next()) {
                val mimeType = res.getString("MIMETYPE")
                val size = res.getLong("SIZE")
                cover = DataUtils.createResource(pIdCover, mimeType, size)
            }
        }
        return cover
    }

    fun countByType(type: MediaType): Int {
        var total = 0
        SQLUtils.openConnection().use { cnx ->
            try {
                logger.fine(String.format("SELECT count by TYPE (%d)",
                        type.index))
                SQLUtils.execute(cnx, COUNT_BY_TYPE, arrayOf(type.index)).use { res ->
                    if (res != null && res.next()) {
                        total = res.getInt(1)
                    }
                }
            } catch (exc: SQLException) {
                logger.log(Level.SEVERE, "Erreur COUNT", exc)
            }
        }
        return total
    }

    fun clean() {
        SQLUtils.openConnection().use { cnx ->
            try {
                SQLUtils.execute(cnx, SELECT_PATHS).use { res ->
                    if (res != null) {
                        while (res.next()) {
                            val id = res.getInt("ID")
                            val strPath = res.getString("PATH")
                            val path = Paths.get(strPath)
                            if (!Files.exists(path)) {
                                logger.fine(String.format("DELETE (%d, %s)", id, strPath))
                                SQLUtils.execute(cnx, DELETE, arrayOf(id))
                            }
                        }
                    }
                }
            } catch (exc: SQLException) {
                logger.log(Level.SEVERE, "Error CLEAN", exc)
            }
        }
    }

    fun insertConf(cnx: Connection, name: String?, value: String?) {
        SQLUtils.execute(cnx, INSERT_CONF, arrayOf(name, value))
    }

    fun updateConf(name: String?, value: String?) {
        SQLUtils.openConnection().use { cnx ->
            SQLUtils.execute(cnx, UPDATE, arrayOf(name, value))
        }
    }

    fun selectConf(): Map<String, String> {
        val params: MutableMap<String, String> = HashMap()
        SQLUtils.openConnection().use { cnx ->
            try {
                SQLUtils.execute(cnx, SELECT).use { res ->
                    while (res!!.next()) {
                        params[res.getString(1)] = res.getString(2)
                    }
                }
            } catch (exc: SQLException) {
                logger.log(Level.SEVERE, "Error SELECT", exc)
            }
        }
        return params
    }

    @JvmStatic
    fun selectConf(name: String?): String {
        var value = "error"
        SQLUtils.openConnection().use { cnx ->
            try {
                SQLUtils.execute(cnx, SELECT_BY_NAME, arrayOf(name)).use { res ->
                    while (res!!.next()) {
                        value = res.getString(1)
                    }
                }
            } catch (exc: SQLException) {
                logger.log(Level.SEVERE, "Error SELECT", exc)
            }
        }
        return value
    }

}