package org.processing.java.gradle

import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.internal.file.Deleter
import org.gradle.work.InputChanges
import processing.mode.java.preproc.PdePreprocessor
import java.io.File
import java.io.ObjectOutputStream
import java.util.concurrent.Callable
import java.util.jar.JarFile
import javax.inject.Inject


// TODO: Generate sourcemaps
abstract class PDETask : SourceTask() {


    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    @get:SkipWhenEmpty
    open val stableSources: FileCollection = project.files(Callable<Any> { this.source })

    @OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @get:Input
    @get:Optional
    var workingDir: String? = null

    @get:Input
    var sketchName: String = "processing"

    @get:Input
    @get:Optional
    var sketchBook: String? = null

    @OutputFile
    val sketchMetaData = project.objects.fileProperty()

    init{
        outputDirectory.convention(project.layout.buildDirectory.dir("generated/pde"))
        sketchMetaData.convention(project.layout.buildDirectory.file("processing/sketch"))
    }

    data class SketchMeta(
        val sketchName: String,
        val sketchRenderer: String?,
        val importStatements: List<String>
    ) : java.io.Serializable

    @TaskAction
    fun execute() {
        // TODO: Allow pre-processor to run on individual files (future)
        // TODO: Only compare file names from both defined roots (e.g. sketch.pde and folder/sketch.pde should both be included)

        // Using stableSources since we can only run the pre-processor on the full set of sources
        val combined = stableSources
            .files
            .groupBy { it.name }
            .map { entry ->
                entry.value.maxByOrNull { it.lastModified() }!!
            }
            .joinToString("\n"){
                 it.readText()
         }
        val javaFile = File(outputDirectory.get().asFile, "$sketchName.java").bufferedWriter()

        val meta = PdePreprocessor
            .builderFor(sketchName)
            .setTabSize(4)
            .build()
            .write(javaFile, combined)

        javaFile.flush()
        javaFile.close()

        val sketchMeta = SketchMeta(
            sketchName = sketchName,
            sketchRenderer = meta.sketchRenderer,
            importStatements = meta.importStatements.map { importStatement -> importStatement.packageName }
        )

        val metaFile = ObjectOutputStream(sketchMetaData.get().asFile.outputStream())
        metaFile.writeObject(sketchMeta)
        metaFile.close()
    }

    @get:Inject
    open val deleter: Deleter
        get() {
            throw UnsupportedOperationException("Decorator takes care of injection")
        }
}