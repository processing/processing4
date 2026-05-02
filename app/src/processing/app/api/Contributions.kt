package processing.app.api

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import processing.app.Platform
import processing.app.api.Sketch.Companion.getSketches
import java.io.File

class Contributions: SuspendingCliktCommand(){
    override fun help(context: Context) = "Manage Processing contributions"
    override suspend fun run() {
        System.setProperty("java.awt.headless", "true")
    }
    init {
        subcommands(Examples())
    }

    class Examples: SuspendingCliktCommand("examples") {
        override fun help(context: Context) = "Manage Processing examples"
        override suspend fun run() {
        }
        init {
            subcommands(ExamplesList())
        }
    }

    class ExamplesList: SuspendingCliktCommand("list") {
        val serializer = Json {
            prettyPrint = true
        }

        override fun help(context: Context) = "List all examples"
        override suspend fun run() {
            Platform.init()

            val json = serializer.encodeToString(listAllExamples())
            println(json)
        }

        companion object {
            /**
             * Get all example sketch folders
             * @return List of example sketch folders
             */
            fun listAllExamples(): List<Sketch.Companion.Folder?> {
                // TODO: Decouple modes listing from `Base` class, defaulting to Java mode for now
                // TODO: Allow the user to change the sketchbook location
                // TODO: Currently blocked since `Base.getSketchbookFolder()` is not available in headless mode
                // TODO: Make non-blocking
                // TODO: Add tests

                val sketchbookFolder = Platform.getDefaultSketchbookFolder()
                val resourcesDir = System.getProperty("compose.application.resources.dir")

                val javaMode = "$resourcesDir/modes/java"

                val javaModeExamples = File("$javaMode/examples")
                    .listFiles()
                    ?.map { getSketches(it) }
                    ?: emptyList()

                val javaModeLibrariesExamples = File("$javaMode/libraries")
                    .listFiles { it.isDirectory }
                    ?.map { library ->
                        val properties = library.resolve("library.properties")
                        val name = findNameInProperties(properties) ?: library.name

                        val libraryExamples = getSketches(library.resolve("examples"))
                        Sketch.Companion.Folder(
                            type = "folder",
                            name = name,
                            path = library.absolutePath,
                            mode = "java",
                            children = libraryExamples?.children ?: emptyList(),
                            sketches = libraryExamples?.sketches ?: emptyList()
                        )
                    } ?: emptyList()
                val javaModeLibraries = Sketch.Companion.Folder(
                    type = "folder",
                    name = "Libraries",
                    path = "$javaMode/libraries",
                    mode = "java",
                    children = javaModeLibrariesExamples,
                    sketches = emptyList()
                )

                val contributedLibraries = sketchbookFolder.resolve("libraries")
                    .listFiles { it.isDirectory }
                    ?.map { library ->
                        val properties = library.resolve("library.properties")
                        val name = findNameInProperties(properties) ?: library.name
                        // Get library name from library.properties if it exists
                        val libraryExamples = getSketches(library.resolve("examples"))
                        Sketch.Companion.Folder(
                            type = "folder",
                            name = name,
                            path = library.absolutePath,
                            mode = "java",
                            children = libraryExamples?.children ?: emptyList(),
                            sketches = libraryExamples?.sketches ?: emptyList()
                        )
                    } ?: emptyList()

                val contributedLibrariesFolder = Sketch.Companion.Folder(
                    type = "folder",
                    name = "Contributed Libraries",
                    path = sketchbookFolder.resolve("libraries").absolutePath,
                    mode = "java",
                    children = contributedLibraries,
                    sketches = emptyList()
                )

                val contributedExamples = sketchbookFolder.resolve("examples")
                    .listFiles { it.isDirectory }
                    ?.map {
                        val properties = it.resolve("examples.properties")
                        val name = findNameInProperties(properties) ?: it.name

                        val sketches = getSketches(it.resolve("examples"))
                        Sketch.Companion.Folder(
                            type = "folder",
                            name,
                            path = it.absolutePath,
                            mode = "java",
                            children = sketches?.children ?: emptyList(),
                            sketches = sketches?.sketches ?: emptyList(),
                        )
                    }
                    ?: emptyList()
                val contributedExamplesFolder = Sketch.Companion.Folder(
                    type = "folder",
                    name = "Contributed Examples",
                    path = sketchbookFolder.resolve("examples").absolutePath,
                    mode = "java",
                    children = contributedExamples,
                    sketches = emptyList()
                )

                return javaModeExamples + javaModeLibraries + contributedLibrariesFolder + contributedExamplesFolder
            }

            private fun findNameInProperties(properties: File): String? {
                if (!properties.exists()) return null

                return properties.readLines().firstNotNullOfOrNull { line ->
                    line.split("=", limit = 2)
                        .takeIf { it.size == 2 && it[0].trim() == "name" }
                        ?.let { it[1].trim() }
                }
            }
        }
    }
}