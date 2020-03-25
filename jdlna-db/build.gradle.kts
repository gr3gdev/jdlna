plugins {
    kotlin("jvm") version "1.3.61"
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
    implementation("com.sleepycat:je:+")
    implementation("org.codehaus.jackson:jackson-mapper-asl:+")
}