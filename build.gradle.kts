plugins {
    kotlin("jvm") version libs.versions.kotlin apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.mavenPublish) apply false

    alias(libs.plugins.versions)
}

// Set the build directory to not /build to prevent accidental deletion through the clean action
// Can be deleted after the migration to Gradle is complete
layout.buildDirectory = file(".build")

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
