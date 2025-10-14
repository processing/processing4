package org.processing.java.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
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

    @InputFiles
    val libraryDirectories: ConfigurableFileCollection = project.files()

    @OutputFile
    val librariesMetaData: RegularFileProperty = project.objects.fileProperty()

    init{
        librariesMetaData.convention { project.gradle.gradleUserHomeDir.resolve("common/processing/libraries") }
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
        val output = libraryDirectories.flatMap { librariesDirectory ->
            if (!librariesDirectory.exists()) {
                logger.error("Libraries directory (${librariesDirectory.path}) does not exist. Libraries will not be imported.")
                return@flatMap emptyList()
            }
            val libraries = librariesDirectory
                .listFiles { file -> file.isDirectory }
                ?.map { folder ->
                    // Find all the jars in the sketchbook
                    val jars = folder.resolve("library")
                        .listFiles{ file -> file.extension == "jar" }
                        ?.map{ file ->

                            // Inside each jar, look for the defined classes
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

            return@flatMap libraries
        }
        val meta = ObjectOutputStream(librariesMetaData.get().asFile.outputStream())
        meta.writeObject(output)
        meta.close()
    }
}