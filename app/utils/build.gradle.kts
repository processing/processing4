plugins {
    id("java")
    kotlin("jvm") version libs.versions.kotlin
}

group = "processing.utils"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jogamp.org/deployment/maven") }
}

sourceSets{
    main{
        java{
            srcDirs("src")
        }
        kotlin{
            srcDirs("src")
        }
        resources{
            srcDirs("resources", listOf("languages", "fonts", "theme").map { "../../build/shared/lib/$it" })
        }
    }
    test{
        kotlin{
            srcDirs("src/test")
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.jna)
    implementation(libs.jnaplatform)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}