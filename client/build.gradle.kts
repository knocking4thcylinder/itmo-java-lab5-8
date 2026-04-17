plugins {
    application
}

dependencies {
    implementation(project(":common"))
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
    mainClass = "org.client.ClientApp"
    applicationName = "client"
}
