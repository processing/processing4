package processing.app.api

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import processing.app.Language
import processing.app.Platform
import processing.app.Preferences
import java.io.File

class SketchCommand: SuspendingCliktCommand("sketch"){
    override fun help(context: Context) = "Manage a Processing sketch"
    override suspend fun run() {

    }
    init {
        subcommands(Format())
    }

    class Format: SuspendingCliktCommand("format"){
        override fun help(context: Context) = "Format a Processing sketch"
        val file by argument("file")
            .help("Path to the sketch file to format")
        val inPlace by option("-i","--inplace")
            .flag()
            .help("Format the file in place, otherwise prints to stdout")

        override suspend fun run(){
            try {
                Platform.init()
                Language.init()
                Preferences.init()

                // run in headless mode
                System.setProperty("java.awt.headless", "true")

                val clazz = Class.forName("processing.mode.java.AutoFormat")
                // Indirect invocation since app does not depend on java mode
                val formatter = clazz
                    .getDeclaredConstructor()
                    .newInstance()

                val method = clazz.getMethod("format", String::class.java)
                val code = File(file).readText()

                val formatted = method.invoke(formatter, code) as String
                if(inPlace) {
                    File(file).writeText(formatted)
                    return
                }
                println(formatted)
            } catch (e: Exception) {
                throw InternalError("Failed to invoke main method", e)
            }
        }
    }
}