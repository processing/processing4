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
import processing.app.ui.Start

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
            LegacyCLI(args)
        )
        .main(args)
}

class LSP: SuspendingCliktCommand("lsp"){
    override fun help(context: Context) = "Start the Processing Language Server"
    override suspend fun run(){
        try {
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
            if (arguments.contains("--build")) {
                System.setProperty("java.awt.headless", "true")
            }

            Class.forName("processing.mode.java.Commander")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, arguments.toTypedArray())
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}
