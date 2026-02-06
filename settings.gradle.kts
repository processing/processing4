rootProject.name = "processing"

pluginManagement {
    includeBuild("gradle/plugins")
}

include(
    "core",
    "core:examples",
    "app",
    "app:utils",
    "java",
    "java:preprocessor",
    "java:gradle",
    "java:libraries:dxf",
    "java:libraries:io",
    "java:libraries:net",
    "java:libraries:pdf",
    "java:libraries:serial",
    "java:libraries:svg",
)