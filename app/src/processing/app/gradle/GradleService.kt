package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.gradle.tooling.BuildLauncher
import processing.app.Base
import processing.app.Language
import processing.app.Messages
import processing.app.Mode
import processing.app.Platform
import processing.app.Preferences
import processing.app.Sketch
import processing.app.ui.Editor
import java.io.*
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

// TODO: Test offline mode, gradle seems to be included as not needed to be downloaded.
// TODO: Test running examples
// TODO: Report failures to the console
// TODO: Highlight errors in the editor
// TODO: Stop running sketches if modern build system is turned off

// TODO: ---- FUTURE ----
// TODO: Improve progress tracking
// TODO: PoC new debugger/tweak mode
// TODO: Allow for plugins to skip gradle entirely / new modes
// TODO: Add background building
// TODO: Track build speed (for analytics?)

/*
* The gradle service runs the gradle tasks and manages the gradle connection
* It will create the necessary build files for gradle to run
* Then it will kick off a new GradleJob to run the tasks
* GradleJob manages the gradle build and connects the debugger
*/
class GradleService(
    val mode: Mode,
    val editor: Editor?,
) {
    val active = mutableStateOf(Preferences.getBoolean("run.use_gradle"))

    var sketch: Sketch? = null

    val jobs = mutableStateListOf<GradleJob>()
    val workingDir = createTempDirectory()
    val debugPort = (30_000..60_000).random()

    // TODO: Add support for present
    fun run(){
        stopActions()

        val job = GradleJob()
        job.service = this
        job.configure = {
            setup()
            forTasks("run")
        }
        jobs.add(job)
        job.start()
    }

    fun export(){
        stopActions()

        val job = GradleJob()
        job.service = this
        job.configure = {
            setup()
            forTasks("runDistributable")
        }
        jobs.add(job)
        job.start()
    }

    fun stop(){
        stopActions()
    }

    fun stopActions(){
        jobs
            .forEach(GradleJob::cancel)
    }

    private fun setupGradle(): MutableList<String> {
        val sketch = sketch ?: throw IllegalStateException("Sketch is not set")

        val unsaved = sketch.code
            .map { code ->
                val file = workingDir.resolve("unsaved/${code.fileName}")
                file.parent.toFile().mkdirs()
                // If tab is marked modified save it to the working directory
                // Otherwise delete the file so we don't compile with old code
                if(code.isModified){
                    file.writeText(code.documentText)
                }else{
                    file.deleteIfExists()
                }
                return@map code.fileName
            }

        val variables = mapOf(
            "group" to System.getProperty("processing.group", "org.processing"),
            "version" to Base.getVersionName(),
            "sketchFolder" to sketch.folder.absolutePath,
            "sketchbook" to Base.getSketchbookFolder(),
            "workingDir" to workingDir.toAbsolutePath().toString(),
            "settings" to Platform.getSettingsFolder().absolutePath.toString(),
            "unsaved" to unsaved.joinToString(","),
            "debugPort" to debugPort.toString(),
            "fullscreen" to false, // TODO: Implement
            "display" to 1, // TODO: Implement
            "external" to true,
            "location" to null, // TODO: Implement
            "editor.location" to editor?.location?.let { "${it.x},${it.y}" },
            //"awt.disable" to false,
            //"window.color" to "0xFF000000", // TODO: Implement
            //"stop.color" to "0xFF000000", // TODO: Implement
            "stop.hide" to false, // TODO: Implement
            "sketch.folder" to sketch.folder.absolutePath,
        )
        val repository = Platform.getContentFile("repository").absolutePath.replace("""\""", """\\""")

        val initGradle = workingDir.resolve("init.gradle.kts").apply {
            val content = """
                beforeSettings{
                    pluginManagement {
                        repositories {
                            maven { url = uri("$repository") }
                            gradlePluginPortal()
                        }
                    }
                }
                allprojects{
                    repositories {
                        maven { url = uri("$repository") }
                        mavenCentral()
                    }
                }
            """.trimIndent()

            writeText(content)
        }


        val buildGradle = sketch.folder.resolve("build.gradle.kts")
        val generate = buildGradle.let {
            if(!it.exists()) return@let true

            val contents = it.readText()
            if(!contents.contains("@processing-auto-generated")) return@let false

            val version = contents.substringAfter("version=").substringBefore("\n")
            if(version != Base.getVersionName()) return@let true

            val modeTitle = contents.substringAfter("mode=").substringBefore(" ")
            if(this.mode.title != modeTitle) return@let true

            return@let Base.DEBUG
        }
        if (generate) {
            Messages.log("build.gradle.kts outdated or not found in ${sketch.folder}, creating one")
            val header = """
                // @processing-auto-generated mode=${mode.title} version=${Base.getVersionName()}
                //
                """.trimIndent()

            val instructions = Language.text("gradle.instructions")
                .split("\n")
                .joinToString("\n") { "// $it" }

            val configuration =  """
                
                plugins{
                    id("org.processing.java") version "${Base.getVersionName()}"
                }
            """.trimIndent()
            val content = "${header}\n${instructions}\n${configuration}"
            buildGradle.writeText(content)
        }
        val settingsGradle = sketch.folder.resolve("settings.gradle.kts")
        if (!settingsGradle.exists()) {
            settingsGradle.createNewFile()
        }

        val arguments = mutableListOf("--init-script", initGradle.toAbsolutePath().toString())
        if (!Base.DEBUG) arguments.add("--quiet")
        arguments.addAll(variables.entries
            .filter { it.value != null }
            .map { "-Pprocessing.${it.key}=${it.value}" }
        )

        return arguments
    }


    private fun BuildLauncher.setup(extraArguments: List<String> = listOf()) {
        setJavaHome(Platform.getJavaHome())

        val arguments = setupGradle()
        arguments.addAll(extraArguments)
        withArguments(*arguments.toTypedArray())
    }

    // Hooks for java to check if the Gradle service is running since mutableStateOf is not accessible in java
    fun getEnabled(): Boolean {
        return active.value
    }
    fun setEnabled(active: Boolean) {
        this.active.value = active
    }
}