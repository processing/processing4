package processing.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

abstract class SignResourcesTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    abstract val resourcesPath: DirectoryProperty

    init {
        group = "compose desktop"
        description = "Signs macOS resources (binaries and libraries) for distribution"
    }

    @TaskAction
    fun signResources() {
        val resourcesDir = resourcesPath.get().asFile
        val jars = mutableListOf<File>()

        // Copy Info.plist if present
        project.fileTree(resourcesDir)
            .matching { include("**/Info.plist") }
            .singleOrNull()
            ?.let { file ->
                project.copy {
                    from(file)
                    into(resourcesDir)
                }
            }

        // Extract JARs to temporary directories for signing
        project.fileTree(resourcesDir) {
            include("**/*.jar")
            exclude("**/*.jar.tmp/**")
        }.forEach { file ->
            val tempDir = file.parentFile.resolve("${file.name}.tmp")
            project.copy {
                from(project.zipTree(file))
                into(tempDir)
            }
            file.delete()
            jars.add(tempDir)
        }

        // Sign all binaries and native libraries
        project.fileTree(resourcesDir) {
            include("**/bin/**")
            include("**/*.jnilib")
            include("**/*.dylib")
            include("**/*aarch64*")
            include("**/*x86_64*")
            include("**/*ffmpeg*")
            include("**/ffmpeg*/**")
            exclude("jdk/**")
            exclude("*.jar")
            exclude("*.so")
            exclude("*.dll")
        }.forEach { file ->
            execOperations.exec {
                commandLine(
                    "codesign",
                    "--timestamp",
                    "--force",
                    "--deep",
                    "--options=runtime",
                    "--sign",
                    "Developer ID Application",
                    file
                )
            }
        }

        // Repackage JARs after signing
        jars.forEach { file ->
            FileOutputStream(File(file.parentFile, file.nameWithoutExtension)).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    file.walkTopDown().forEach { fileEntry ->
                        if (fileEntry.isFile) {
                            val zipEntryPath = fileEntry.relativeTo(file).path
                            val entry = ZipEntry(zipEntryPath)
                            zos.putNextEntry(entry)
                            fileEntry.inputStream().use { input ->
                                input.copyTo(zos)
                            }
                            zos.closeEntry()
                        }
                    }
                }
            }
            file.deleteRecursively()
        }

        // Clean up Info.plist
        File(resourcesDir, "Info.plist").delete()
    }
}
