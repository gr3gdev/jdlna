buildscript {
    extra.apply {
        set("java.version", "1.8")
        set("cling.version", "2.1.2")
    }
}

plugins {
    kotlin("jvm") version "1.3.61" apply false
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
