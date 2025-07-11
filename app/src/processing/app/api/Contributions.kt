package processing.app.api

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import processing.app.Base
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

            val examplesFolder = Base.getSketchbookExamplesFolder()

            // TODO: Decouple modes listing from `Base` class, defaulting to Java mode for now
            val resourcesDir = System.getProperty("compose.application.resources.dir")
            val javaMode = "$resourcesDir/modes/java"
            val javaModeExamples = "$javaMode/examples"

            val javaExamples = getSketches(File(javaModeExamples))

            val json = serializer.encodeToString(listOf(javaExamples))
            println(json)

            // Build-in examples for each mode
            // Get examples for core libraries

            // Examples downloaded in the sketchbook
            // Library contributions
            // Mode examples
        }


    }
}