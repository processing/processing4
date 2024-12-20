rootProject.name = "processing"
include(
    "core",
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

buildscript {
    repositories {
        mavenCentral()
    }
}