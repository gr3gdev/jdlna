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
    implementation("org.eclipse.jetty:jetty-client:8.+")
    implementation("org.eclipse.jetty:jetty-servlet:8.+")
    implementation(project(":jdlna-bean"))
    implementation(project(":jdlna-h2"))
    implementation(project(":jdlna-service"))
    implementation("org.fourthline.cling:cling-core:${rootProject.extra.get("cling.version")}")
}