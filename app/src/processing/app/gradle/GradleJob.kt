package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.sun.jdi.VirtualMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.gradle.tooling.BuildCancelledException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.problems.ProblemEvent
import org.gradle.tooling.events.problems.Severity
import org.gradle.tooling.events.problems.internal.DefaultSingleProblemEvent
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskStartEvent
import org.gradle.tooling.events.task.TaskSuccessResult
import processing.app.Base.DEBUG
import processing.app.Base.getSketchbookFolder
import processing.app.Base.getVersionName
import processing.app.Language.text
import processing.app.Messages
import processing.app.Platform
import processing.app.Platform.getContentFile
import processing.app.Platform.getSettingsFolder
import processing.app.Sketch
import processing.app.gradle.Log.Companion.startLogServer
import processing.app.ui.Editor
import processing.app.ui.EditorStatus
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

/*
* The gradle job runs the gradle tasks and manages the gradle connection
 */
class GradleJob(
    vararg val tasks: String,
    val workingDir: Path,
    val sketch: Sketch,
    val editor: Editor? = null,
){
    enum class State{
        NONE,
        BUILDING,
        RUNNING,
        ERROR,
        DONE
    }

    val debugPort = (30_000..60_000).random()
    val logPort = debugPort + 1
    val errPort = logPort + 1

    val state = mutableStateOf(State.NONE)
    val vm = mutableStateOf<VirtualMachine?>(null)
    val problems = mutableStateListOf<ProblemEvent>()
    val jobs = mutableStateListOf<Job>()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val cancel = GradleConnector.newCancellationTokenSource()


    /*
    Set up the gradle build launcher with the necessary configuration
    This includes setting the working directory, the tasks to run,
    and the arguments to pass to gradle.
    Create the necessary build files if they do not exist.
     */
    private fun BuildLauncher.setupGradle(extraArguments: List<String> = listOf()) {
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
        // This allows us to run the gradle plugin that has been bundled with the editor
        // TODO: Add the plugin repositories if they are defined
        val initGradle = workingDir.resolve("init.gradle.kts").apply {
            val content = """
                beforeSettings{
                    pluginManagement {
                        repositories {
                            maven("$repository")
                            gradlePluginPortal()
                        }
                    }
                }
                allprojects{
                    repositories {
                        maven("$repository")
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
            if(sketch.mode.title != modeTitle) return@let true

            return@let DEBUG
        }
        if (generate) {
            Messages.log("build.gradle.kts outdated or not found in ${sketch.folder}, creating one")
            val header = """
                // @processing-auto-generated mode=${sketch.mode.title} version=${getVersionName()}
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
            val content = "${header}\n${instructions}\n\n${configuration}"
            buildGradle.writeText(content)
        }
        // Create and empty settings.gradle.kts file in the sketch folder
        val settingsGradle = sketchFolder.resolve("settings.gradle.kts")
        if (!settingsGradle.exists()) {
            settingsGradle.createNewFile()
        }
        // Collect the arguments to pass to gradle
        val arguments = mutableListOf("--init-script", initGradle.toAbsolutePath().toString())
        // Hide Gradle output from the console if not in debug mode
        if(!DEBUG) arguments += "--quiet"

        if(copy) arguments += listOf("--project-dir", sketchFolder.absolutePath)

        arguments += variables.entries
            .filter { it.value != null }
            .map { "-Pprocessing.${it.key}=${it.value}" }

        arguments += extraArguments

        withArguments(*arguments.toTypedArray())

        forTasks(*tasks)

        // TODO: Instead of shipping Processing with a build-in JDK we should download the JDK through Gradle
        setJavaHome(Platform.getJavaHome())
        withCancellationToken(cancel.token())
    }

    /*
    Start the gradle job and run the tasks
     */
    fun start() {
        launchJob {
            handleExceptions {
                state.value = State.BUILDING

                // Connect Gradle, configure the build and run it
                GradleConnector.newConnector()
                    .forProjectDirectory(sketch.folder)
                    .apply {
                        editor?.statusMessage("Connecting to Gradle", EditorStatus.NOTICE)
                        // TODO: Remove when switched to classic confinement within Snap
                        if (System.getenv("SNAP_USER_COMMON") != null) {
                            useGradleUserHomeDir(getSettingsFolder().resolve("gradle"))
                        }
                    }
                    .connect()
                    .apply {
                        editor?.statusMessage("Building sketch", EditorStatus.NOTICE)
                    }
                    .newBuild()
                    .apply {
                        if (DEBUG) {
                            setStandardOutput(System.out)
                            setStandardError(System.err)
                        }

                        setupGradle()

                        addStateListener()
                        addLogserver()
                        addDebugging()

                    }
                    .run()
            }
        }
    }


    /*
    Cancel the gradle job and all the jobs that were launched in this scope
     */
    fun cancel(){
        cancel.cancel()
        jobs.forEach(Job::cancel)
    }

    /*
    Add a job to the scope and add it to the list of jobs so we can cancel it later
     */
    private fun launchJob(block: suspend CoroutineScope.() -> Unit){
        val job = scope.launch { block() }
        jobs.add(job)
    }


    /*
    Handle exceptions that occur during the build process and inform the user about them
     */
    private fun handleExceptions(action: () -> Unit){
        try{
            action()
        }catch (e: Exception){
            val causesList = mutableListOf<Throwable>()
            var cause: Throwable? = e

            while (cause != null && cause.cause != cause) {
                causesList.add(cause)
                cause = cause.cause
            }

            val errors = causesList.joinToString("\n") { it.message ?: "Unknown error" }

            val skip = listOf(BuildCancelledException::class)

            if (skip.any { it.isInstance(e) }) {
                Messages.log("Gradle job error: $errors")
                return
            }

            if(state.value == State.RUNNING){
                Messages.log("Gradle job error: $errors")
                return
            }

            // An error occurred during the build process

            System.err.println(errors)
            editor?.statusError(causesList.last().message)
        }finally {
            state.value = State.DONE
            vm.value = null
        }
    }

    // TODO: Move to separate file?
    /*
    Add a progress listener to the build launcher
    to track the progress of the build and update the editor status accordingly
     */
    private fun BuildLauncher.addStateListener(){
        addProgressListener(ProgressListener { event ->
            if(event is TaskStartEvent) {
                editor?.statusMessage("Running task: ${event.descriptor.name}", EditorStatus.NOTICE)
                when(event.descriptor.name) {
                    ":run" -> {
                        state.value = State.RUNNING
                        Messages.log("Start run")
                        editor?.toolbar?.activateRun()
                    }
                }

            }
            if(event is TaskFinishEvent) {
                if(event.result is TaskSuccessResult){
                    editor?.statusMessage("Finished task ${event.descriptor.name}", EditorStatus.NOTICE)
                }

                when(event.descriptor.name){
                    ":run"->{
                        state.value = State.DONE
                        editor?.toolbar?.deactivateRun()
                        editor?.toolbar?.deactivateStop()
                    }
                }
            }
            if(event is DefaultSingleProblemEvent) {


                problems.add(event)

                val skip = listOf(
                    "mutating-the-dependencies-of-configuration-implementation-after-it-has-been-resolved-or-consumed-this-behavior-has-been-deprecated",
                    "mutating-the-dependencies-of-configuration-runtimeonly-after-it-has-been-resolved-or-consumed-this-behavior-has-been-deprecated"
                )
                if(skip.any { event.definition.id.name.contains(it) }) {
                    Messages.log(event.toString())
                    return@ProgressListener
                }

                if(event.definition.severity == Severity.ADVICE) {
                    Messages.log(event.toString())
                    return@ProgressListener
                }
                // TODO: Show the error on the location if it is available
                // TODO: This functionality should be provided by the mode
                /*
                We have 6 lines to display the error in the editor.
                 */

                val error = event.definition.id.displayName
                editor?.statusError(error)
                System.err.println("Problem: $error")
                state.value = State.ERROR

                val message = """
                    Context: ${event.contextualLabel.contextualLabel}
                    Solutions: ${event.solutions.joinToString("\n\t") { it.solution }}
                """
                    .trimIndent()

                println(message)
            }
        })
    }

    /*
    Start log servers for the standard output and error streams
    This allows us to capture the output of Processing and display it in the editor
    Whilst keeping the gradle output separate
     */
    fun BuildLauncher.addLogserver(){
        launchJob {
            startLogServer(logPort, System.out)
        }
        launchJob{
            startLogServer(errPort, System.err)
        }
    }

    /*
    Connected a debugger to the gradle run task
    This allows us to debug the sketch while it is running
     */
    fun BuildLauncher.addDebugging() {
        addProgressListener(ProgressListener { event ->
            if (event !is TaskStartEvent) return@ProgressListener
            if (event.descriptor.name != ":run") return@ProgressListener

            launchJob {
                val debugger = Debugger.connect(debugPort) ?: return@launchJob
                vm.value = debugger
                val exceptions = Exceptions(debugger, editor)
                exceptions.listen()
            }

        })
    }
}