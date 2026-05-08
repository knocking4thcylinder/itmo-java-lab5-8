plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "org.App"
    applicationName = "client"
}
