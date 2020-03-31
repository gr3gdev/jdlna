package org.dev.gr3g.jdlna.dao

import org.h2.tools.RunScript
import java.io.IOException
import java.io.StringReader
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object SQLUtils {

    /** Logger.  */
    private val logger = Logger.getLogger(SQLUtils::class.java.name)

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

    fun execute(cnx: Connection,
                request: String): ResultSet? {
        var result: ResultSet? = null
        try {
            val reader = StringReader(request)
            result = RunScript.execute(cnx, reader)
            reader.close()
        } catch (exc: SQLException) {
            logger.log(Level.SEVERE, "Error RUNSCRIPT", exc)
        }
        return result
    }

    fun execute(cnx: Connection?, request: String,
                params: Array<Any?>): ResultSet? {
        var result: ResultSet? = null
        try {
            params.forEachIndexed { idx, param ->
                if (param is String) {
                    params[idx] = param.replace("'", "''")
                }
            }
            val reader = StringReader(String.format(request, *params))
            if (cnx != null) {
                result = RunScript.execute(cnx, reader)
            }
        } catch (exc: SQLException) {
            logger.log(Level.SEVERE, "Error RUNSCRIPT", exc)
        }
        return result
    }

}