package org.processing.java.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.ObjectInputStream

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
                            dependencies.add(jar.path)
                            return@import
                        }
                    }
                }
            }
        }
        project.dependencies.add("implementation",  project.files(dependencies) )

        // TODO: Add only if user is compiling for P2D or P3D
        // Add JOGL and Gluegen dependencies
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:jogl-all-main:2.5.0")
        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt-main:2.5.0")

        // TODO: Only add the native dependencies for the platform the user is building for
        // MacOS specific native dependencies
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:jogl-all:2.5.0:natives-macosx-universal")
        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.5.0:natives-macosx-universal")

        // Windows specific native dependencies
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:jogl-all:2.5.0:natives-windows-amd64")
        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.5.0:natives-windows-amd64")

        // Linux specific native dependencies
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:jogl-all:2.5.0:natives-linux-amd64")
        project.dependencies.add("runtimeOnly", "org.jogamp.gluegen:gluegen-rt:2.5.0:natives-linux-amd64")

        // NativeWindow dependencies for all platforms
        project.dependencies.add("implementation", "org.jogamp.jogl:nativewindow:2.5.0")
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:nativewindow:2.5.0:natives-macosx-universal")
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:nativewindow:2.5.0:natives-windows-amd64")
        project.dependencies.add("runtimeOnly", "org.jogamp.jogl:nativewindow:2.5.0:natives-linux-amd64")
    }
}