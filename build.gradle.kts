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

val enableWebGPU = findProperty("enableWebGPU")?.toString()?.toBoolean() ?: false
val javaVersion = if (enableWebGPU) "25" else "17"
val kotlinJvmTarget = if (enableWebGPU) {
    org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
} else {
    org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(kotlinJvmTarget)
        }
    }

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
            }
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
