plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.2.20"
}

gradlePlugin {
    plugins {
        create("processing.library") {
            id = "org.processing.library"
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