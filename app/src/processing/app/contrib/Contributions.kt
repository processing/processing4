package processing.app.contrib

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import processing.app.Base
import java.io.File
import kotlinx.serialization.json.*


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
        @Serializable
        data class Sketch(
            val type: String = "sketch",
            val name: String,
            val path: String,
            val mode: String = "java",
        )

        @Serializable
        data class Folder(
            val type: String = "folder",
            val name: String,
            val path: String,
            val mode: String = "java",
            val children: List<Folder> = emptyList(),
            val sketches: List<Sketch> = emptyList()
        )

        val serializer = Json{
            prettyPrint = true
        }

        override fun help(context: Context) = "List all examples"
        override suspend fun run() {

            val examplesFolder = Base.getSketchbookExamplesFolder()

            // TODO: Decouple modes listing from `Base` class, defaulting to Java mode for now
            val resourcesDir = System.getProperty("compose.application.resources.dir")
            val javaMode = "$resourcesDir/modes/java"
            val javaModeExamples = "$javaMode/examples"

            val javaExamples = getExamples(File(javaModeExamples))

            val json = serializer.encodeToString(listOf(javaExamples))
            println(json)

            // Build-in examples for each mode
            // Get examples for core libraries

            // Examples downloaded in the sketchbook
            // Library contributions
            // Mode examples
        }

        suspend fun getExamples(file: File): Folder {
            val name = file.name
            val (sketchesFolders, childrenFolders) = file.listFiles().partition { isExampleFolder(it) }

            val children = childrenFolders.map { getExamples(it) }
            val sketches = sketchesFolders.map { Sketch(name = it.name, path = it.absolutePath) }
            return Folder(
                name = name,
                path = file.absolutePath,
                children = children,
                sketches = sketches
            )
        }
        fun isExampleFolder(file: File): Boolean {
            return file.isDirectory && file.listFiles().any { it.isFile && it.name.endsWith(".pde") }
        }
    }
}

