package org.dev.gr3g.jdlna.db

import com.sleepycat.je.*
import org.codehaus.jackson.map.ObjectMapper
import org.dev.gr3g.jdlna.bean.MediaFile
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
class JdlnaDB(url: String, name: String) {

    private val database: Database?
    private val environment: Environment?

    /**
     * Select the [Path] of a media file.
     * @param id ID of the media file
     * @return [Path]
     */
    fun select(id: String): Path? {
        var path: Path? = null
        val theKey = DatabaseEntry(
                id.toByteArray(StandardCharsets.UTF_8))
        val theData = DatabaseEntry()
        if (database!![null, theKey, theData, LockMode.DEFAULT] == OperationStatus.SUCCESS) {
            val strPath = String(theData.data,
                    StandardCharsets.UTF_8)
            path = Paths.get(strPath)
        }
        return path
    }

    /**
     * Select all children files.
     * @param parent ID parent
     * @return List
     */
    fun selectByParent(parent: String): List<MediaFile> {
        val children: MutableList<MediaFile> = ArrayList()
        val theKey = DatabaseEntry(
                parent.toByteArray(StandardCharsets.UTF_8))
        val theData = DatabaseEntry()
        var cursor: Cursor? = null
        try {
            cursor = database!!.openCursor(null, null)
            val mapper = ObjectMapper()
            while (cursor.getNext(theKey, theData,
                            LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                val file = mapper.readValue(theData.data,
                        MediaFile::class.java)
                children.add(file)
            }
        } catch (exc: IOException) {
            LOGGER.log(Level.SEVERE, "Error SELECT BY PARENT", exc)
        } finally {
            cursor?.close()
        }
        return children
    }

    /**
     * Insert a media file.
     * @param data Media file
     */
    fun insert(data: MediaFile) {
        val mapper = ObjectMapper()
        try {

            // ID parent : file
            val keyParent = data.parent
            val valueParent = mapper.writeValueAsString(data)
            val theKey = DatabaseEntry(
                    keyParent!!.toByteArray(StandardCharsets.UTF_8))
            val theValue = DatabaseEntry(
                    valueParent.toByteArray(StandardCharsets.UTF_8))
            database!!.put(null, theKey, theValue)

            // ID file : PATH
            val keyFile = data.id
            val valueFile = data.path
            val theKeyFile = DatabaseEntry(
                    keyFile!!.toByteArray(StandardCharsets.UTF_8))
            val theValuFile = DatabaseEntry(
                    valueFile!!.toByteArray(StandardCharsets.UTF_8))
            database.put(null, theKeyFile, theValuFile)
        } catch (exc: IOException) {
            LOGGER.log(Level.SEVERE, "Error INSERT MediaFile", exc)
        }
    }

    /** Close the database.  */
    fun close() {
        database?.close()
        environment?.close()
    }

    companion object {
        private val LOGGER = Logger
                .getLogger(JdlnaDB::class.java.name)
    }

    /**
     * Constructor.
     * @param url URL to save file
     * @param name Name of database
     */
    init {
        val envConfig = EnvironmentConfig()
        envConfig.allowCreate = true
        environment = Environment(File(url), envConfig)

        // Open the database.
        // Create it if it does not already exist.
        val dbConfig = DatabaseConfig()
        dbConfig.allowCreate = true
        database = environment.openDatabase(null, name, dbConfig)
    }
}