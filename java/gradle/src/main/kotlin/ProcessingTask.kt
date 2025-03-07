package org.processing.java.gradle

import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.internal.file.Deleter
import org.gradle.work.ChangeType
import org.gradle.work.FileChange
import org.gradle.work.InputChanges
import processing.mode.java.preproc.PdePreprocessor
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.util.concurrent.Callable
import javax.inject.Inject

abstract class ProcessingTask() : SourceTask() {
     @get:OutputDirectory
    var outputDirectory: File? = null

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    @get:SkipWhenEmpty
    open val stableSources: FileCollection = project.files(Callable<Any> { this.source })

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val files: MutableSet<File> = HashSet()
        if (inputChanges.isIncremental) {
            var rebuildRequired = true
            for (fileChange: FileChange in inputChanges.getFileChanges(stableSources)) {
                if (fileChange.fileType == FileType.FILE) {
                    if (fileChange.changeType == ChangeType.REMOVED) {
                        rebuildRequired = true
                        break
                    }
                    files.add(fileChange.file)
                }
            }
            if (rebuildRequired) {
                try {
                    outputDirectory?.let { deleter.ensureEmptyDirectory(it) }
                } catch (ex: IOException) {
                    throw UncheckedIOException(ex)
                }
                files.addAll(stableSources.files)
            }
        } else {
            files.addAll(stableSources.files)
        }

        val name = project.layout.projectDirectory.asFile.name.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val combined = files.joinToString("\n") { it.readText() }
        File(outputDirectory, "$name.java")
            .bufferedWriter()
            .use { out ->
                val meta = PdePreprocessor
                    .builderFor(name)
                    .build()
                    .write(out, combined)


                // TODO: Only import the libraries that are actually used
                val importStatement = meta.importStatements
            }
    }

    @get:Inject
    open val deleter: Deleter
        get() {
            throw UnsupportedOperationException("Decorator takes care of injection")
        }
}