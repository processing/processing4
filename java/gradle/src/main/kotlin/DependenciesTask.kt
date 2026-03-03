package org.processing.java.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.ObjectInputStream

/*
* The DependenciesTask resolves the dependencies for the sketch based on the libraries used
 */
abstract class DependenciesTask: DefaultTask() {
    @InputFile
    val librariesMetaData: RegularFileProperty = project.objects.fileProperty()

    @get:OutputDirectory
    val outputJarsDirectory: DirectoryProperty = project.objects.directoryProperty()

    @InputFile
    val sketchMetaData: RegularFileProperty = project.objects.fileProperty()

    init{
        librariesMetaData.convention(project.layout.buildDirectory.file("processing/libraries"))
        sketchMetaData.convention(project.layout.buildDirectory.file("processing/sketch"))
        outputJarsDirectory.convention(project.layout.buildDirectory.dir("processing/libs"))
    }

    @TaskAction
    fun execute() {
        val sketchMetaFile = sketchMetaData.get().asFile
        val librariesMetaFile = librariesMetaData.get().asFile

        val libraries = librariesMetaFile.inputStream().use { input ->
            ObjectInputStream(input).readObject() as ArrayList<LibrariesTask.Library>
        }

        val sketch = sketchMetaFile.inputStream().use { input ->
            ObjectInputStream(input).readObject() as PDETask.SketchMeta
        }

        val dependencies = mutableSetOf<File>()

        // Loop over the import statements in the sketch and import the relevant jars from the libraries
        sketch.importStatements.forEach import@{ statement ->
            libraries.forEach { library ->
                library.jars.forEach { jar ->
                    jar.classes.forEach { className ->
                        if (className.startsWith(statement)) {
                            dependencies.addAll(library.jars.map { it.path })
                            return@import
                        }
                    }
                }
            }
        }
        // Copy discovered jars to the output directory so they can be wired into the
        // compilation classpath at configuration time (Gradle 9 forbids mutating
        // configurations after they have been observed/resolved).
        val outputDir = outputJarsDirectory.get().asFile
        outputDir.deleteRecursively()
        outputDir.mkdirs()
        dependencies.forEach { jar ->
            jar.copyTo(outputDir.resolve(jar.name), overwrite = true)
        }
    }
}