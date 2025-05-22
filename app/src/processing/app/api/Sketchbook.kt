package processing.app.api

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import processing.app.Base
import processing.app.api.Sketch.Companion.getSketches

class Sketchbook: SuspendingCliktCommand() {
    override fun help(context: Context) = "Manage the sketchbook"
    override suspend fun run() {
        System.setProperty("java.awt.headless", "true")
    }
    init {
        subcommands(SketchbookList())
    }
    class SketchbookList: SuspendingCliktCommand("list") {
        override fun help(context: Context) = "List all sketches"
        override suspend fun run() {
            val sketchbookFolder = Base.getSketchbookFolder()

            val sketches = getSketches(sketchbookFolder)
        }
    }
}