plugins {
    kotlin("jvm")
    application
    id("com.palantir.docker") version "0.25.0"
    id("com.palantir.docker-run") version "0.25.0"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = rootProject.extra.get("java.version") as String
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = rootProject.extra.get("java.version") as String
    }
    application {
        mainClassName = "org.dev.gr3g.jdlna.App"
    }
    docker {
        files(distZip.get().outputs, file("Dockerfile"))
        name = "jdlna"
        setDockerfile(file("Dockerfile"))
    }
    dockerRun {
        name = "jdlna"
        image = "jdlna"
        clean = true
        daemonize = true
        ports("9300:9300")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.fourthline.cling:cling-core:${rootProject.extra.get("cling.version")}")
    implementation(project(":jdlna-bean"))
    implementation(project(":jdlna-h2"))
    implementation(project(":jdlna-service"))
    implementation(project(":jdlna-jetty"))
}
