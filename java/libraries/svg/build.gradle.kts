import java.net.URI

// Batik configuration
val batikVersion = "1.19"
val batikZip = "batik-bin-$batikVersion.zip"
val batikJarName = "batik-all-$batikVersion.jar"
val batikJar = file("library/batik.jar")

// URLs
val batikUrl = "https://dlcdn.apache.org/xmlgraphics/batik/binaries/$batikZip"
val batikBackupUrl = "https://download.processing.org/batik/$batikZip"

plugins {
    id("java-library")
}


val downloadBatik by tasks.registering {
    outputs.file(batikJar)

    doLast {
        if (!batikJar.exists()) {
            batikJar.parentFile.mkdirs() // Ensures 'library' directory exists
            project.logger.lifecycle("Batik JAR not found at ${batikJar.absolutePath}. Downloading...")

            val urlsToTry = listOf(batikUrl, batikBackupUrl)
            var downloaded = false

            for (currentUrl in urlsToTry) {
                try {
                    project.logger.info("Attempting to download Batik from: $currentUrl")
                    // Create temp file in the build directory for easier cleanup if something goes wrong
                    val tempZipFile = project.layout.buildDirectory.file("tmp/${batikZip}").get().asFile
                    tempZipFile.parentFile.mkdirs()
                    tempZipFile.deleteOnExit() // In case of abnormal termination

                    URI(currentUrl).toURL().openStream().use { input ->
                        tempZipFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    project.logger.info("Downloaded $batikZip to ${tempZipFile.absolutePath}")

                    project.copy {
                        from(project.zipTree(tempZipFile))
                        include("**/lib/$batikJarName") // e.g. batik-1.19/lib/batik-all-1.19.jar
                        into(batikJar.parentFile) // Extract to 'library' directory
                        rename { _ -> batikJar.name } // Rename the single matched file to 'batik.jar'
                    }

                    project.logger.lifecycle("Extracted $batikJarName to ${batikJar.absolutePath}")
                    tempZipFile.delete() // Clean up the downloaded zip
                    downloaded = true
                    break // Success
                } catch (e: Exception) {
                    project.logger.warn("Failed to download/extract Batik from $currentUrl: ${e.message} (${e.javaClass.simpleName})")
                }
            }

            if (!downloaded) {
                if (batikJar.exists()){
                    project.logger.warn("Batik download failed from all sources, but a previous version exists at ${batikJar.absolutePath}. Using existing.")
                } else {
                    throw GradleException("Failed to download Batik from all sources and no local copy found at ${batikJar.path}.")
                }
            }
        } else {
            project.logger.info("Batik JAR already exists at ${batikJar.absolutePath}. Skipping download.")
        }
    }

}

val coreJar = file("../../../core/library/core.jar")

tasks.register("checkCore") {
    doFirst {

        if (!coreJar.exists()) {
            throw GradleException("Missing core.jar at $coreJar. Please build the core module first.")
        }
    }
}



dependencies {
    implementation(files(coreJar))
    implementation(files(batikJar))

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(downloadBatik)
    options.encoding = "UTF-8"
}



tasks.named<Jar>("jar") {
    archiveBaseName.set("svg")
    destinationDirectory.set(file("library"))
}

tasks.named<Delete>("clean") {
    delete("bin", "library/svg.jar")
}