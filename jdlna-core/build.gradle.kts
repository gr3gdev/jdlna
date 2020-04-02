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
    implementation("org.fourthline.cling:cling-support:${rootProject.extra.get("cling.version")}")
    implementation("org.fourthline.cling:cling-core:${rootProject.extra.get("cling.version")}")
    implementation("com.h2database:h2:1.+")
    testImplementation("junit:junit:4.12")
}
