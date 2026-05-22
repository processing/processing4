package processing.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class CargoBuildTask : DefaultTask() {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    abstract val cargoWorkspaceDir: DirectoryProperty

    @get:Input
    abstract val manifestPath: Property<String>

    @get:Input
    abstract val release: Property<Boolean>

    @get:Input
    abstract val cargoPath: Property<String>

    @get:OutputFile
    abstract val outputLibrary: RegularFileProperty

    init {
        group = "rust"
        description = "Builds Rust library using cargo"

        // release by default
        release.convention(true)
    }

    @TaskAction
    fun build() {
        val buildType = if (release.get()) "release" else "debug"
        logger.lifecycle("Building Rust library ($buildType mode)...")

        val args = mutableListOf("build")
        if (release.get()) {
            args.add("--release")
        }
        args.add("--manifest-path")
        args.add(manifestPath.get())

        execOperations.exec {
            workingDir = cargoWorkspaceDir.get().asFile
            commandLine = listOf(cargoPath.get()) + args
        }
    }
}
