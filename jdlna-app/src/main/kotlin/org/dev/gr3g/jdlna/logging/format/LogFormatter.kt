package org.dev.gr3g.jdlna.logging.format

import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * @author Gregory Tardivel
 */
class LogFormatter : Formatter() {

    private val date = Date()

    override fun format(record: LogRecord): String {
        val level = record.level
        val message = record.message
        val className = record.sourceClassName
        val method = record.sourceMethodName
        val exc = record.thrown
        val time = record.millis
        date.time = time
        return if (level == Level.SEVERE) {
            var error = exc.message
            try {
                val writer = StringWriter()
                exc.printStackTrace(PrintWriter(writer))
                writer.close()
                error = writer.toString()
            } catch (ioexc: IOException) {
            }
            String.format(FORMAT_EXC, date, level.name, className,
                    method, message, error)
        } else {
            String.format(FORMAT, date, level.name, className,
                    method, message)
        }
    }

    companion object {
        private const val FORMAT = "%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL [%2\$s] - %3\$s.%4\$s - %5\$s\n"
        private const val FORMAT_EXC = "%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL [%2\$s] - %3\$s.%4\$s - %5\$s :\n%6\$s\n"
    }
}