plugins {
    id("com.gradle.plugin-publish") version "2.0.0"
    kotlin("jvm") version "2.2.20"
}

gradlePlugin {
    website = "https://processing.org/"
    vcsUrl = "https://github.com/processing/processing4"
    plugins {
        create("processing.library") {
            id = project.properties.getOrElse("publishingGroup", { "org.processing" }).toString() + ".library"
            displayName = "Processing Library Plugin"
            description = "Gradle plugin for building Processing libraries"
            tags = listOf("processing", "library", "dsl")
            implementationClass = "ProcessingLibraryPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}