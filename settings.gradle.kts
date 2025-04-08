pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
    }
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "processing"
rootProject.name = "processing"
include(
    "core",
    "core:examples",
    "app",
    "java",
    "java:preprocessor",
    "java:libraries:dxf",
    "java:libraries:io",
    "java:libraries:net",
    "java:libraries:pdf",
    "java:libraries:serial",
    "java:libraries:svg",
)