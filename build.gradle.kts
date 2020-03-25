buildscript {
    extra.apply {
        set("java.version", "1.8")
        set("cling.version", "2.1.2")
    }
}

group = "com.github.gr3gdev"
version = "0.1.0"

allprojects {

    repositories {
        jcenter()
        mavenCentral()
        maven("http://4thline.org/m2")
    }

}