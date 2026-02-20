package processing.app.api

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import processing.app.Platform
import processing.app.Preferences
import processing.app.api.Sketch.Companion.getSketches
import java.io.File

class Sketchbook: SuspendingCliktCommand() {


    override fun help(context: Context) = "Manage the sketchbook"
    override suspend fun run() {
        System.setProperty("java.awt.headless", "true")
    }
    init {
        subcommands(SketchbookList())
    }


    class SketchbookList: SuspendingCliktCommand("list") {
        val serializer = Json {
            prettyPrint = true
        }

        override fun help(context: Context) = "List all sketches"
        override suspend fun run() {
            Platform.init()
            // TODO: Allow the user to change the sketchbook location
            // TODO: Currently blocked since `Base.getSketchbookFolder()` is not available in headless mode
            val sketchbookFolder = Platform.getDefaultSketchbookFolder()

            val sketches = getSketches(sketchbookFolder) {
                !listOf(
                    "android",
                    "modes",
                    "tools",
                    "examples",
                    "libraries"
                ).contains(it.name)
            }
            val json = serializer.encodeToString(listOf(sketches))
            println(json)
        }
    }
}