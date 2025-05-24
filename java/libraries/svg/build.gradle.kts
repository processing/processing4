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


dependencies {
    implementation(project(":core"))
    implementation(files(batikJar))
}

tasks.register("checkCore") {
    doFirst {
        if (!coreJar.exists()) {
            throw GradleException("Missing core.jar at $coreJar. Please build the core module first.")
        }
    }
}

tasks.register<Jar>("svgJar") {
    dependsOn("checkCore", "classes")
    archiveBaseName.set("svg")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

// Batik configuration
val batikVersion = "1.19"
val batikZip = "batik-bin-$batikVersion.zip"
val batikJarName = "batik-all-$batikVersion.jar"
val batikJar = file("library/batik.jar")

// URLs
val batikUrl = "https://dlcdn.apache.org/xmlgraphics/batik/binaries/$batikZip"
val batikBackupUrl = "https://download.processing.org/batik/$batikZip"

// Storing a copy locally in case the main URL goes dead
val downloadBatik by tasks.registering {
    outputs.file(batikJar)

    doLast {
        if (batikJar.exists()) {
            logger.lifecycle("Batik JAR already exists at ${batikJar.absolutePath}, skipping download.")
            return@doLast
        }

        val urlsToTry = listOf(batikUrl, batikBackupUrl)
        val zipFile = layout.buildDirectory.file(batikZip).get().asFile

        zipFile.parentFile.mkdirs()

        val success = urlsToTry.any { url ->
            try {
                logger.lifecycle("Trying to download Batik from $url")
                URI(url).toURL().openStream().use { input ->
                    zipFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                logger.lifecycle("Downloaded Batik zip to ${zipFile.absolutePath}")

                copy {
                    from(zipTree(zipFile))
                    include("**/lib/$batikJarName")
                    into(batikJar.parentFile)
                    rename { batikJar.name }
                }

                logger.lifecycle("Extracted $batikJarName to ${batikJar.absolutePath}")
                zipFile.delete()
                true
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


tasks.named<JavaCompile>("compileJava") {
    dependsOn(downloadBatik, "checkCore")
    options.encoding = "UTF-8"
    classpath += files(batikJar)

}

tasks.named<Jar>("jar") {
    archiveBaseName.set("svg")
    destinationDirectory.set(file("library"))
    from(sourceSets.main.get().output)
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
    }
}

// Cleanup
tasks.named<Delete>("clean") {
    delete("bin", "library/svg.jar")
}