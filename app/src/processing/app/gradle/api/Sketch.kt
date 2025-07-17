package processing.app.gradle.api

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import processing.app.Base
import processing.app.Platform
import processing.app.Preferences
import processing.app.contrib.ModeContribution
import processing.app.gradle.GradleJob
import processing.app.gradle.GradleService

class Sketch : SuspendingCliktCommand("sketch") {
    init {
        subcommands(
            Run()
        )
    }

    override fun help(context: Context): String {
        return """Manage sketches in the Processing environment."""
    }

    override suspend fun run() {
        System.setProperty("java.awt.headless", "true")
    }

    class Run : SuspendingCliktCommand(name = "run") {
        val sketch by option("--sketch", help = "The sketch to run")
                        .required()

        val mode by option("--mode", help = "The mode to use for running the sketch (only java is supported for now)")

        override fun help(context: Context): String {
            return "Run the Processing sketch."
        }

        override suspend fun run() {
            Base.setCommandLine()
            Platform.init()
            Preferences.init()
            Base.locateSketchbookFolder()

            // TODO: Support modes other than Java
            val mode = ModeContribution.load(
                null, Platform.getContentFile("modes/java"),
                "processing.mode.java.JavaMode"
            ).mode ?: throw IllegalStateException("Java mode not found")

            System.setProperty("java.awt.headless", "false")

            val service = GradleService(mode,null)
            service.sketch.value = processing.app.Sketch(sketch, mode)
            service.run()

            // TODO: Use an async way to wait for the job to finish
            //Wait for the service to finish
            while (service.jobs.any { it.state.value != GradleJob.State.DONE }) {
                Thread.sleep(100)
            }
        }
    }
}