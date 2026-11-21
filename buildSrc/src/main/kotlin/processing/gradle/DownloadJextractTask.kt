package processing.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.net.URI

abstract class DownloadJextractTask : DefaultTask() {

    @get:Input
    abstract val jextractVersion: Property<String>

    @get:Input
    abstract val platform: Property<String>

    @get:OutputDirectory
    abstract val jextractDir: DirectoryProperty

    @get:Internal
    abstract val downloadTarball: RegularFileProperty

    init {
        group = "rust"
        description = "Downloads and extracts jextract for the current platform"
    }

    @TaskAction
    fun download() {
        val version = jextractVersion.get()
        val plat = platform.get()
        val fileName = "openjdk-$version" + "_${plat}_bin.tar.gz"
        val downloadUrl = "https://download.java.net/java/early_access/jextract/22/6/$fileName"
        val tarFile = downloadTarball.get().asFile

        if (!tarFile.exists()) {
            logger.lifecycle("Downloading jextract from $downloadUrl")
            try {
                tarFile.outputStream().use { output ->
                    URI.create(downloadUrl).toURL().openStream().use { input ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                throw GradleException("Failed to download jextract: ${e.message}", e)
            }
        }

        val extractDir = jextractDir.get().asFile
        logger.lifecycle("Extracting jextract to ${extractDir.parent}")
        project.copy {
            from(project.tarTree(tarFile))
            into(extractDir.parent)
        }

        logger.lifecycle("jextract extracted to: $extractDir")
    }
}
