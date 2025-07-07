package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.gradle.tooling.BuildLauncher
import processing.app.Base.DEBUG
import processing.app.Base.getSketchbookFolder
import processing.app.Base.getVersionName
import processing.app.Language.text
import processing.app.Messages
import processing.app.Mode
import processing.app.Platform
import processing.app.Platform.getContentFile
import processing.app.Platform.getSettingsFolder
import processing.app.Preferences
import processing.app.Sketch
import processing.app.ui.Editor
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

// TODO: Test offline mode, gradle seems to be included as not needed to be downloaded.
// TODO: Test running examples
// TODO: Report failures to the console
// TODO: Highlight errors in the editor

// TODO: ---- FUTURE ----
// TODO: Improve progress tracking and show it in the UI
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
    val logPort = debugPort + 1
    val errPort = logPort + 1

    fun run(){
        startJob("run")
    }

    fun export(){
        startJob("runDistributable")
    }

    fun stop(){
        stopJobs()
    }

    private fun startJob(vararg tasks: String) {
        if(!active.value) return
        editor?.let { println(text("gradle.using_gradle"))  }

        val job = GradleJob()
        job.service = this
        job.configure = {
            setupGradle()
            forTasks(tasks.joinToString(" "))
        }
        jobs.add(job)
        job.start()
    }

    private fun stopJobs(){
        jobs.forEach(GradleJob::cancel)
    }

    private fun BuildLauncher.setupGradle(extraArguments: List<String> = listOf()) {
        val sketch = sketch ?: throw IllegalStateException("Sketch is not set")
        val copy = sketch.isReadOnly || sketch.isUntitled
        val sketchFolder = if(copy) workingDir.resolve("sketch").toFile() else sketch.folder
        if(copy){
            // If the sketch is read-only, we copy it to the working directory
            // This allows us to run the sketch without modifying the original files
            sketch.folder.copyRecursively(sketchFolder, overwrite = true)
        }
        // Save the unsaved code into the working directory for gradle to compile
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
        // Collect the variables to pass to gradle
        val variables = mapOf(
            "group" to System.getProperty("processing.group", "org.processing"),
            "version" to getVersionName(),
            "sketchFolder" to sketchFolder,
            "sketchbook" to getSketchbookFolder(),
            "workingDir" to workingDir.toAbsolutePath().toString(),
            "settings" to getSettingsFolder().absolutePath.toString(),
            "unsaved" to unsaved.joinToString(","),
            "debugPort" to debugPort.toString(),
            "logPort" to logPort.toString(),
            "errPort" to errPort.toString(),
            "fullscreen" to System.getProperty("processing.fullscreen", "false").equals("true"),
            "display" to 1, // TODO: Implement
            "external" to true,
            "location" to null, // TODO: Implement
            "editor.location" to editor?.location?.let { "${it.x},${it.y}" },
            //"awt.disable" to false,
            //"window.color" to "0xFF000000", // TODO: Implement
            //"stop.color" to "0xFF000000", // TODO: Implement
            "stop.hide" to false, // TODO: Implement
        )
        val repository = getContentFile("repository").absolutePath.replace("""\""", """\\""")
        // Create the init.gradle.kts file in the working directory
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
        // Create the build.gradle.kts file in the sketch folder
        val buildGradle = sketchFolder.resolve("build.gradle.kts")
        val generate = buildGradle.let {
            if(!it.exists()) return@let true

            val contents = it.readText()
            if(!contents.contains("@processing-auto-generated")) return@let false

            val version = contents.substringAfter("version=").substringBefore("\n")
            if(version != getVersionName()) return@let true

            val modeTitle = contents.substringAfter("mode=").substringBefore(" ")
            if(mode.title != modeTitle) return@let true

            return@let DEBUG
        }
        if (generate) {
            Messages.log("build.gradle.kts outdated or not found in ${sketch.folder}, creating one")
            val header = """
                // @processing-auto-generated mode=${mode.title} version=${getVersionName()}
                //
                """.trimIndent()

            val instructions = text("gradle.instructions")
                .split("\n")
                .joinToString("\n") { "// $it" }

            val configuration =  """
                
                plugins{
                    id("org.processing.java") version "${getVersionName()}"
                }
            """.trimIndent()
            val content = "${header}\n${instructions}\n${configuration}"
            buildGradle.writeText(content)
        }
        // Create the settings.gradle.kts file in the sketch folder
        val settingsGradle = sketchFolder.resolve("settings.gradle.kts")
        if (!settingsGradle.exists()) {
            settingsGradle.createNewFile()
        }
        // Collect the arguments to pass to gradle
        val arguments = mutableListOf("--init-script", initGradle.toAbsolutePath().toString())
        if (!DEBUG) arguments.add("--quiet")
        if(copy){
            arguments += listOf("--project-dir", sketchFolder.absolutePath)
        }

        arguments.addAll(variables.entries
            .filter { it.value != null }
            .map { "-Pprocessing.${it.key}=${it.value}" }
        )
        arguments.addAll(extraArguments)

        withArguments(*arguments.toTypedArray())

        // TODO: Instead of shipping Processing with a build-in JDK we should download the JDK through Gradle
        setJavaHome(Platform.getJavaHome())
    }

    // Hooks for java to check if the Gradle service is running since mutableStateOf is not accessible in java
    fun getEnabled(): Boolean {
        return active.value
    }
    fun setEnabled(active: Boolean) {
        if(!active) stopJobs()
        this.active.value = active
    }
}