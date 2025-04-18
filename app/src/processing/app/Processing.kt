package processing.app

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import processing.app.ui.Start

// TODO: Allow Start to run on no args
// TODO: Modify InstallCommander to use the new structure
// TODO: Move dependency to gradle toml
// TODO: Add the options/arguments for Base arguments
class Processing(val args: Array<String>): SuspendingCliktCommand(name = "Processing"){
    override suspend fun run() {
        if(currentContext.invokedSubcommand == null){
            Start.main(args)
        }
    }
}

suspend fun main(args: Array<String>) = Processing(args)
    .subcommands(
        LSP(args),
        LegacyCLI(args)
    )
    .main(args)


class LSP(val args: Array<String>): SuspendingCliktCommand("lsp"){
    override fun help(context: Context) = "Start the Processing Language Server"
    override suspend fun run(){
        try {
            // Indirect invocation since app does not depend on java mode
            Class.forName("processing.mode.java.lsp.PdeLanguageServer")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, *arrayOf<Any>(args))
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}

class LegacyCLI(val args: Array<String>): SuspendingCliktCommand(name = "cli"){
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
        val cliArgs = args.filter { it != "cli" }.toTypedArray()
        try {
            // Indirect invocation since app does not depend on java mode
            Class.forName("processing.mode.java.Commander")
                .getMethod("main", Array<String>::class.java)
                .invoke(null, *arrayOf<Any>(cliArgs))
        } catch (e: Exception) {
            throw InternalError("Failed to invoke main method", e)
        }
    }
}