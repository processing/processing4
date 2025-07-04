package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.sun.jdi.VirtualMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import processing.app.Base
import processing.app.Messages
import processing.app.Platform
import processing.app.ui.EditorStatus

class GradleJob{
    enum class State{
        NONE,
        BUILDING,
        RUNNING,
        ERROR,
        DONE
    }

    var service: GradleService? = null
    var configure: BuildLauncher.() -> Unit = {}

    val state = mutableStateOf(State.NONE)
    val vm = mutableStateOf<VirtualMachine?>(null)
    val problems = mutableStateListOf<ProblemEvent>()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val cancel = GradleConnector.newCancellationTokenSource()

    fun start() {
        val folder = service?.sketch?.folder ?: throw IllegalStateException("Sketch folder is not set")
        scope.launch {
            try {
                state.value = State.BUILDING
                service?.editor?.statusMessage("Building sketch", EditorStatus.NOTICE)

                GradleConnector.newConnector()
                    .forProjectDirectory(folder)
                    .apply {
                        // TODO: Remove when switched to classic confinement within Snap
                        if(System.getenv("SNAP_USER_COMMON") != null){
                            useGradleUserHomeDir(Platform.getSettingsFolder().resolve("gradle"))
                        }
                    }
                    .connect()
                    .newBuild()
                    .apply {
                        configure()
                        withCancellationToken(cancel.token())
                        addStateListener()
                        addDebugging()
                        setStandardOutput(System.out)
                        if(Base.DEBUG) {
                            setStandardError(System.err)
                        }
                        run()
                    }
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
                    return@launch
                }

                if(state.value != State.BUILDING){
                    Messages.log("Gradle job error: $errors")
                    return@launch
                }

                // An error occurred during the build process

                System.err.println(errors)
                service?.editor?.statusError(cause?.message)
            }finally {
                state.value = State.DONE
                vm.value = null
            }
        }
    }

    fun cancel(){
        cancel.cancel()
    }
    private fun BuildLauncher.addStateListener(){
        addProgressListener(ProgressListener { event ->
            if(event is TaskStartEvent) {
                service?.editor?.statusMessage("Running task: ${event.descriptor.name}", EditorStatus.NOTICE)
                when(event.descriptor.name) {
                    ":run" -> {
                        state.value = State.RUNNING
                        Messages.log("Start run")
                        service?.editor?.toolbar?.activateRun()
                    }
                }

            }
            if(event is TaskFinishEvent) {
                if(event.result is TaskSuccessResult){
                    service?.editor?.statusMessage("Finished task ${event.descriptor.name}", EditorStatus.NOTICE)
                }

                when(event.descriptor.name){
                    ":run"->{
                        state.value = State.DONE
                        service?.editor?.toolbar?.deactivateRun()
                        service?.editor?.toolbar?.deactivateStop()
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
                /*
                We have 6 lines to display the error in the editor.
                 */

                val error = event.definition.id.displayName
                service?.editor?.statusError(error)
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

    fun BuildLauncher.addDebugging() {
        addProgressListener(ProgressListener { event ->
            if (event !is TaskStartEvent) return@ProgressListener
            if (event.descriptor.name != ":run") return@ProgressListener

            scope.launch {
                val debugger = Debugger.connect(service?.debugPort) ?: return@launch
                vm.value = debugger
                val exceptions = Exceptions(debugger, service?.editor)
                exceptions.listen()
            }

        })
    }
}