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
    implementation(files(coreJar))
}

tasks.register("checkCore") {
    doFirst {
        if (!coreJar.exists()) {
            throw GradleException("Missing core.jar at $coreJar. Please build the core module first.")
        }
    }
}

tasks.register<Jar>("dxfJar") {
    dependsOn("checkCore", "classes")
    archiveBaseName.set("dxf")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

tasks.register("clean") {
    doLast {
        delete("build", "library/dxf.jar")
    }
}
