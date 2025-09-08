package org.processing.java.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.ObjectOutputStream
import java.util.jar.JarFile

/*
The libraries task scans the sketchbook libraries folder for all the libraries
This task stores the resulting information in a file that can be used later to resolve dependencies
 */
abstract class LibrariesTask : DefaultTask() {

    // TODO: Allow multiple directories
    @InputDirectory
    @Optional
    val librariesDirectory: DirectoryProperty = project.objects.directoryProperty()

    @OutputFile
    val librariesMetaData: RegularFileProperty = project.objects.fileProperty()

    init{
        librariesMetaData.convention(project.layout.buildDirectory.file("processing/libraries"))
    }

    data class Jar(
        val path: File,
        val classes: List<String>
    ) : java.io.Serializable

    data class Library(
        val jars: List<Jar>
    ) : java.io.Serializable

    @TaskAction
    fun execute() {
        if (!librariesDirectory.isPresent) {
            logger.error("Libraries directory is not set. Libraries will not be imported.")
            val meta = ObjectOutputStream(librariesMetaData.get().asFile.outputStream())
            meta.writeObject(arrayListOf<Library>())
            meta.close()
            return
        }
        val libraries = librariesDirectory.get().asFile
            .listFiles { file -> file.isDirectory }
            ?.map { folder ->
                // Find all the jars in the sketchbook
                val jars = folder.resolve("library")
                    .listFiles{ file -> file.extension == "jar" }
                    ?.map{ file ->

                        // Inside of each jar, look for the defined classes
                        val jar = JarFile(file)
                        val classes = jar.entries().asSequence()
                            .filter { entry -> entry.name.endsWith(".class") }
                            .map { entry -> entry.name }
                            .map { it.substringBeforeLast('/').replace('/', '.') }
                            .distinct()
                            .toList()

                        // Return a reference to the jar and its classes
                        return@map Jar(
                            path = file,
                            classes = classes
                        )
                    }?: emptyList()

                // Save the parsed jars and which folder
                return@map Library(
                    jars = jars
                )
            }?: emptyList()

        val meta = ObjectOutputStream(librariesMetaData.get().asFile.outputStream())
        meta.writeObject(libraries)
        meta.close()
    }
}