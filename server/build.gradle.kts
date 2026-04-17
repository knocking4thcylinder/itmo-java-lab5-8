plugins {
    application
}

dependencies {
    implementation(project(":common"))
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

application {
    mainClass = "org.server.ServerApp"
    applicationName = "server"
}
