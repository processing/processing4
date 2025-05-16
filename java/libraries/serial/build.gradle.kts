plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val coreJar = file("../../../core/library/core.jar")

dependencies {

    implementation(project(":core"))
    implementation(files("library/jssc.jar"))
}

tasks.register("checkCore") {
    doFirst {

        if (!coreJar.exists()) {
            throw GradleException("Missing core.jar at $coreJar. Please build the core module first.")
        }
    }
}

tasks.register<Jar>("serialJar") {
    dependsOn("checkCore", "classes")
    archiveBaseName.set("serial")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

