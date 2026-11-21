package processing.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class CargoCleanTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    abstract val cargoWorkspaceDir: DirectoryProperty

    @get:Input
    abstract val manifestPath: Property<String>

    @get:Input
    abstract val cargoPath: Property<String>

    init {
        group = "rust"
        description = "Cleans Rust build artifacts"
    }

    @TaskAction
    fun clean() {
        logger.lifecycle("Cleaning Rust build artifacts...")

        execOperations.exec {
            workingDir = cargoWorkspaceDir.get().asFile
            commandLine(cargoPath.get(), "clean", "--manifest-path", manifestPath.get())
        }
    }
}
