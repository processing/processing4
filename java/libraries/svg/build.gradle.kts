import java.net.URI

plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val coreJar = file("../../../core/library/core.jar")
val batikVersion = "1.19"

// The .zip file to be downloaded
val batikZip = "batik-bin-$batikVersion.zip"

// The .jar that we need from the download
val batikJar = file("library/batik.jar")

// URL for the version of Batik currently supported by this library
val batikUrl = "https://dlcdn.apache.org//xmlgraphics/batik/binaries/$batikZip"

// Storing a "local" copy in case the original link goes dead. When updating
// releases, please upload the new version to download.processing.org.
val batikBackupUrl = "https://download.processing.org/batik/$batikZip"


// Download Batik dependency if not present
// OK to ignore failed downloads if we at least have a version that's local
val downloadBatik by tasks.registering {
    outputs.file(batikJar)

    doLast {
        if (batikJar.exists()) {
            logger.lifecycle("Batik JAR already exists, skipping download.")
            return@doLast
        }

        batikJar.parentFile.mkdirs()
        val zipFile = file(batikZip)
        val urlsToTry = listOf(batikUrl, batikBackupUrl)

        val success = urlsToTry.any { url ->
            try {
                logger.lifecycle("Downloading Batik from $url")
                URI(url).toURL().openStream().use { input ->
                    zipFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                copy {
                    from(zipTree(zipFile)) {
                        include("batik-$batikVersion/lib/batik-all-$batikVersion.jar")
                    }
                    into(batikJar.parentFile)
                    eachFile {
                        path = batikJar.name
                    }
                    includeEmptyDirs = false
                }

                zipFile.delete()

                if (batikJar.exists()) {
                    logger.lifecycle("Successfully extracted Batik JAR to ${batikJar.absolutePath}")
                    true
                } else {
                    logger.error("Extraction failed")
                    false
                }
            } catch (e: Exception) {
                logger.warn("Failed to download from $url: ${e.message}")
                false
            }
        }

        if (!success) {
            throw GradleException("Failed to download Batik from all sources and no local copy found.")
        }
    }
}

tasks.register("checkCore") {
    doFirst {
        if (!coreJar.exists()) {
            throw GradleException("Missing core.jar at $coreJar. Please build the core module first.")
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(files(batikJar))
}

// Compile sources
tasks.named<JavaCompile>("compileJava") {
    dependsOn(downloadBatik, "checkCore")
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {
    dependsOn("checkCore")
    archiveBaseName.set("svg")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

tasks.register<Jar>("svgJar") {
    dependsOn("checkCore", "classes")
    archiveBaseName.set("svg")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

// Clean the build directories
tasks.named<Delete>("clean") {
    delete("bin", "library/svg.jar")
}


tasks.register<Delete>("cleanLibs") {
    delete(batikJar)
}