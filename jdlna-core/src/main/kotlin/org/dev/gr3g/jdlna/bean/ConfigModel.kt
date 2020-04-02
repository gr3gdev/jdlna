package org.dev.gr3g.jdlna.bean

class ConfigModel {

    var video = Node()
    var music = Node()
    var photo = Node()
    var cover = Node()

    class Node {
        var path: String? = null
        var count: Int = 0
    }

}