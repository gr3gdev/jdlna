plugins {
    kotlin("jvm")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = rootProject.extra.get("java.version") as String
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = rootProject.extra.get("java.version") as String
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":jdlna-bean"))
    implementation("org.fourthline.cling:cling-support:${rootProject.extra.get("cling.version")}")
    implementation("com.h2database:h2:1.+")
}
