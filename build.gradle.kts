plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false

    alias(libs.plugins.versions)
}

// Set the build directory to not /build to prevent accidental deletion through the clean action
// Can be deleted after the migration to Gradle is complete
layout.buildDirectory = file(".build")

val enableWebGPU = findProperty("enableWebGPU")?.toString()?.toBoolean() ?: true

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        val javaVersion = if (enableWebGPU) "24" else "17"
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            val kotlinTarget = if (enableWebGPU) {
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
            } else {
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            }
            jvmTarget.set(kotlinTarget)
        }
    }
}

// Configure the dependencyUpdates task
tasks {
    dependencyUpdates {
        gradleReleaseChannel = "current"

        val nonStableKeywords = listOf("alpha", "beta", "rc")

        fun isNonStable(version: String) = nonStableKeywords.any {
            version.lowercase().contains(it)
        }

        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }
}
