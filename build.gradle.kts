plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
}

//allprojects{
//    repositories{
//        maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
//    }
//}

// Set the build directory to not /build to prevent accidental deletion through the clean action
// Can be deleted after the migration to Gradle is complete
layout.buildDirectory = file(".build")

allprojects{
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
    }
}