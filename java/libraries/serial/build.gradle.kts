plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val coreJarPath = layout.projectDirectory.file("../../../core/library/core.jar")
val jsscJarPath = layout.projectDirectory.file("library/jssc.jar")
val binDir = layout.projectDirectory.dir("bin")
val serialJarOutputDir = layout.projectDirectory.dir("library")

dependencies {
    implementation(files(coreJarPath))
    implementation(files(jsscJarPath))
}

tasks.register("checkCore") {
    doFirst {
        if (!coreJarPath.asFile.exists()) {
            throw GradleException("Missing core.jar at $coreJarPath. Please build the core module first.")
        }
    }
}

tasks.register<Jar>("buildSerial") {
    dependsOn("checkCore")
    archiveFileName.set("serial.jar")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

tasks.named<Delete>("clean") {
    delete(binDir)
    delete(serialJarOutputDir.file("serial.jar"))
}
