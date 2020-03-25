package org.dev.gr3g.jdlna.bean.filter

import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * @author Gregory Tardivel
 */
abstract class AbstractFilter : FileFilter {

    protected abstract val extensions: Array<String>

    override fun accept(file: File): Boolean {
        var retour = false
        if (file.isDirectory) {
            retour = true
        } else {
            for (ext in extensions) {
                if (file.name.toLowerCase(Locale.FRENCH)
                                .endsWith(ext.toLowerCase(Locale.FRENCH))) {
                    retour = true
                }
            }
        }
        return retour
    }
}