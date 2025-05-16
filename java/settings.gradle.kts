plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "processing"

include(":app")
include(":core")
include(":java:preprocessor")

include(":java:libraries:dxf")
include(":java:libraries:io")
include(":java:libraries:net")
include(":java:libraries:pdf")
include(":java:libraries:serial")
include(":java:libraries:svg")