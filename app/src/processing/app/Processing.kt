package processing.app

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import processing.app.gradle.api.Sketch
import processing.app.api.Contributions
import processing.app.api.Sketchbook
import processing.app.ui.Start
import java.io.File
import java.util.prefs.Preferences
import kotlin.concurrent.thread

class Processing: SuspendingCliktCommand("processing"){
    val version by option("-v","--version")
        .flag()
        .help("Print version information")

    val sketches by argument()
        .multiple(default = emptyList())
        .help("Sketches to open")

    override fun help(context: Context) = "Start the Processing IDE"
    override val invokeWithoutSubcommand = true
    override suspend fun run() {
        if(version){
            println("processing-${Base.getVersionName()}-${Base.getRevision()}")
            return
        }

        thread {
            // Update the install locations in preferences
            updateInstallLocations()
        }

        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            Start.main(sketches.toTypedArray())
        }
    }
}

suspend fun main(args: Array<String>){
   Processing()
        .subcommands(
            LSP(),
            LegacyCLI(args),
            Sketch(),
            Contributions(),
            Sketchbook()
        )
        .main(args)
}

class LSP: SuspendingCliktCommand("lsp"){
    override fun help(context: Context) = "Start the Processing Language Server"
    override suspend fun run(){
        try {
            // run in headless mode
            System.setProperty("java.awt.headless", "true")

            // Indirect invocation since app does not depend on java mode
            Class.forName("processing.mode.java.lsp.PdeLanguageServer")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, arrayOf<String>())
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}


class LegacyCLI(val args: Array<String>): SuspendingCliktCommand("cli") {
    override val treatUnknownOptionsAsArgs = true

    val help by option("--help").flag()
    val arguments by argument().multiple(default = emptyList())

    override suspend fun run() {
        try {
            System.setProperty("java.awt.headless", "true")

            // Indirect invocation since app does not depend on java mode
            Class.forName("processing.mode.java.Commander")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, arguments.toTypedArray())
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}

fun updateInstallLocations(){
    val preferences = Preferences.userRoot().node("org/processing/app")
    val installLocations = preferences.get("installLocations", "")
        .split(",")
        .dropLastWhile { it.isEmpty() }
        .filter { install ->
            try{
                val (path, version) = install.split("^")
                val file = File(path)
                if(!file.exists() || file.isDirectory){
                    return@filter false
                }
                // call the path to check if it is a valid install location
                val process = ProcessBuilder(path, "--version")
                    .redirectErrorStream(true)
                    .start()
                val exitCode = process.waitFor()
                if(exitCode != 0){
                    return@filter false
                }
                val output = process.inputStream.bufferedReader().readText()
                return@filter output.contains(version)
            } catch (e: Exception){
                false
            }
        }
        .toMutableList()
    val command = ProcessHandle.current().info().command()
    if(command.isEmpty) {
        return
    }
    val installLocation = "${command.get()}^${Base.getVersionName()}"


    // Check if the installLocation is already in the list
    if (installLocations.contains(installLocation)) {
        return
    }

    // Add the installLocation to the list
    installLocations.add(installLocation)

    // Save the updated list back to preferences
    preferences.put("installLocations", java.lang.String.join(",", installLocations))
}
