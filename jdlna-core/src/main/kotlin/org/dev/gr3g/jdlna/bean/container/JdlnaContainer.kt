package org.dev.gr3g.jdlna.bean.container

import org.fourthline.cling.support.model.container.Container

/**
 * @author Gregory Tardivel
 */
class JdlnaContainer(id: String?, parentID: String?,
                     title: String?) : Container() {

    companion object {
        val CLASS = Class("object.container.jdlna")
    }

    init {
        setClazz(CLASS)
        setId(id)
        setParentID(parentID)
        setTitle(title)
    }
}