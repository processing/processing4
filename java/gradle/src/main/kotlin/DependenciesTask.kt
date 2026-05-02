package org.processing.java.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.ObjectInputStream

/*
* The DependenciesTask resolves the dependencies for the sketch based on the libraries used
 */
abstract class DependenciesTask: DefaultTask() {
    @InputFile
    val librariesMetaData: RegularFileProperty = project.objects.fileProperty()

    @InputFile
    val sketchMetaData: RegularFileProperty = project.objects.fileProperty()

    init{
        librariesMetaData.convention(project.layout.buildDirectory.file("processing/libraries"))
        sketchMetaData.convention(project.layout.buildDirectory.file("processing/sketch"))
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
                            dependencies.addAll(library.jars.map { it.path } )
                            return@import
                        }
                    }
                }
            }
        }
        project.dependencies.add("implementation",  project.files(dependencies) )

        // TODO: Mutating the dependencies of configuration ':implementation' after it has been resolved or consumed. This

        // TODO: Add only if user is compiling for P2D or P3D
        // Add JOGL and Gluegen dependencies
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:jogl-all-main:2.5.0")
        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.5.0")

        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()

        val variant = when {
            os.contains("mac") -> "macosx-universal"
            os.contains("win") && arch.contains("64") -> "windows-amd64"
            os.contains("linux") && arch.contains("aarch64") -> "linux-aarch64"
            os.contains("linux") && arch.contains("arm") -> "linux-arm"
            os.contains("linux") && arch.contains("amd64") -> "linux-amd64"
            else -> throw GradleException("Unsupported OS/architecture: $os / $arch")
        }

        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.5.0:natives-$variant")
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:nativewindow:2.5.0:natives-$variant")
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:newt:2.5.0:natives-$variant")
    }
}