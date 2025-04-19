package processing.app

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import processing.app.ui.Start

class Processing: SuspendingCliktCommand("processing"){
    val sketches by argument()
        .multiple(default = emptyList())
        .help("Sketches to open")

    override fun help(context: Context) = "Start the Processing IDE"
    override val invokeWithoutSubcommand = true
    override suspend fun run() {
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
                .invoke(null, *arrayOf<Any>(emptyList<String>()))
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}

class LegacyCLI(val args: Array<String>): SuspendingCliktCommand( "cli"){
    override fun help(context: Context) = "Legacy processing-java command line interface"

    val help by option("--help").flag()
    val build by option("--build").flag()
    val run by option("--run").flag()
    val present by option("--present").flag()
    val sketch: String? by option("--sketch")
    val force by option("--force").flag()
    val output: String? by option("--output")
    val export by option("--export").flag()
    val noJava by option("--no-java").flag()
    val variant: String? by option("--variant")

    override suspend fun run(){
        val cliArgs = args.filter { it != "cli" }
        try {
            if(build){
                System.setProperty("java.awt.headless", "true")
            }
            // Indirect invocation since app does not depend on java mode
            Class.forName("processing.mode.java.Commander")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, *arrayOf<Any>(cliArgs.toTypedArray()))
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}