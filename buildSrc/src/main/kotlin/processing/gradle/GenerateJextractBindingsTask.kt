package processing.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class GenerateJextractBindingsTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val headerFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val targetPackage: Property<String>

    @get:Input
    abstract val jextractPath: Property<String>

    init {
        group = "rust"
        description = "Generates Java Panama FFM bindings from C headers"
    }

    @TaskAction
    fun generate() {
        val outDir = outputDirectory.get().asFile
        outDir.mkdirs()

        logger.lifecycle("Generating Java bindings from ${headerFile.get().asFile}...")

        execOperations.exec {
            commandLine(
                jextractPath.get(),
                "--output", outDir.absolutePath,
                "--target-package", targetPackage.get(),
                headerFile.get().asFile.absolutePath
            )
        }
    }
}
