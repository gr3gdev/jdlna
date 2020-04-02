package org.dev.gr3g.jdlna.dao

import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object ConnectionUtils {

    /** Logger.  */
    private val logger = Logger.getLogger(ConnectionUtils::class.java.name)

    private const val POOL_SIZE = 3
    private val POOL = LinkedList<Connection>()
    private var url: String? = null

    init {
        try {
            val driver = System.getenv("DATABASE_DRIVER") ?: "org.h2.Driver"
            try {
                Class.forName(driver)
            } catch (exc: ClassNotFoundException) {
                logger.log(Level.SEVERE, "Erreur JDBC", exc)
            }
            url = System.getenv("DATABASE_URL")
                    ?: "jdbc:h2:/data/jdlna/content;OPTIMIZE_REUSE_RESULTS=0;MAX_OPERATION_MEMORY=1000;MAX_MEMORY_UNDO=1000"
            for (idx in 0 until POOL_SIZE) {
                POOL.addLast(DriverManager.getConnection(url))
            }
        } catch (exc: IOException) {
            logger.log(Level.SEVERE, "Error loading", exc)
        } catch (exc: SQLException) {
            logger.log(Level.SEVERE, "Error loading", exc)
        }
    }

    fun openConnection(): Connection {
        while (POOL.size < POOL_SIZE) {
            // Conserve pool size
            POOL.addLast(DriverManager.getConnection(url))
        }
        return POOL.pollFirst()
    }

}