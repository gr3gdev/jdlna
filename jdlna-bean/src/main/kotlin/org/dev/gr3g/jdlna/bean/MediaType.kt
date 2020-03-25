package org.dev.gr3g.jdlna.bean

/**
 * @author Gregory Tardivel
 */
enum class MediaType(val index: Int) {

    DIRECTORY(0), VIDEO(1), IMAGE(2), MUSIC(3), COVER(4), UNDEFINED(-1);

    companion object {
        /**
         * @return the index
         */
        fun valueOfIndex(index: Int): MediaType? {
            for (type in values()) {
                if (type.index == index) {
                    return type
                }
            }
            return null
        }
    }

}