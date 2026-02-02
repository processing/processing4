package processing.app

import java.io.File
import java.nio.file.Files
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

    @Test
    fun testSketchWithCustomMainFile(){
        val tempDir = Files.createTempDirectory("cli_custom_main_test")
        try {
            val sketchFolder = tempDir.resolve("TestSketch").toFile()
            sketchFolder.mkdirs()

            // Create custom main file (not matching folder name)
            val customMain = File(sketchFolder, "custom_main.pde")
            customMain.writeText("""
                void setup() {
                  println("Custom main file test");
                }
                
                void draw() {
                  exit();
                }
            """.trimIndent())

            // Create sketch.properties with custom main
            val propsFile = File(sketchFolder, "sketch.properties")
            propsFile.writeText("main=custom_main.pde")

            // Test with CLI
            runCLIWithArguments("cli --sketch=${sketchFolder.absolutePath} --build")
        } finally {
            tempDir.toFile().deleteRecursively()
        }
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
}