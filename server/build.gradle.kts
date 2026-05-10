plugins {
    application
}

import org.gradle.jvm.tasks.Jar

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.ServerApp"
    applicationName = "server"
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Builds a runnable server jar with runtime dependencies."
    archiveClassifier = "all"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.exists() }
            .map { if (it.isDirectory) it else zipTree(it) }
    })
}
