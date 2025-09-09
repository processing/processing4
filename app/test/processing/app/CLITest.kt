package processing.app

import java.io.File
import kotlin.test.Test

/*
This class is used to test the CLI commands of the Processing IDE.
It mostly exists to quickly run CLI commands without having to specify run configurations
or to manually run it on the command line.

In IntelliJ IDEA, it should display runnable arrows next to each test method.
Use this to quickly test the CLI commands.
The output will be displayed in the console after `Running CLI with arguments: ...`.
When developing on the CLI commands, feel free to add more test methods here.
 */
class CLITest {

    @Test
    fun testLSP(){
        runCLIWithArguments("lsp")
    }

    @Test
    fun testLegacyCLI(){
        runCLIWithArguments("cli --help")
    }

    /*
    This function runs the CLI with the given arguments.
     */
    fun runCLIWithArguments(args: String) {
        // TODO: Once Processing PDE correctly builds in IntelliJ IDEA switch over to using the code directly
        // To see if the PDE builds correctly can be tested by running the Processing.kt main function directly in IntelliJ IDEA
        // Set the JAVA_HOME environment variable to the JDK used by the IDE
        println("Running CLI with arguments: $args")
        val process = ProcessBuilder("./gradlew", "run", "--args=$args", "--quiet")
            .directory(File(System.getProperty("user.dir")).resolve("../../../"))
            .inheritIO()

        process.environment().apply {
            put("JAVA_HOME", System.getProperty("java.home"))
        }

        val result = process
            .start()
            .waitFor()
        println("Done running CLI with arguments: $args (Result: $result)")

    }
    fun runCLIAndCapture(vararg args: String): String {
        val argLine = args.joinToString(" ")
        println("Running CLI with arguments: $argLine")

        val process = ProcessBuilder("./gradlew", "run", "--args=$argLine", "--quiet")
            .directory(File(System.getProperty("user.dir")).resolve("../../../"))
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        println("Done running CLI with arguments: $argLine (Exit code: $exitCode)")
        return output
    }

}
