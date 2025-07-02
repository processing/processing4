package processing.app.gradle

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.sun.jdi.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.problems.ProblemEvent
import org.gradle.tooling.events.problems.Severity
import org.gradle.tooling.events.problems.internal.DefaultFileLocation
import org.gradle.tooling.events.problems.internal.DefaultSingleProblemEvent
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskStartEvent
import org.gradle.tooling.events.task.TaskSuccessResult
import processing.app.Base
import processing.app.Messages
import processing.app.ui.EditorStatus
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.lang.IllegalStateException

class GradleJob{
    enum class State{
        NONE,
        BUILDING,
        RUNNING,
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

                GradleConnector.newConnector()
                    .forProjectDirectory(folder)
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
                Messages.log("Error while running: ${e.message} ${e.cause?.message}")
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
                    ":jar"->{
                        state.value = State.NONE
                        Messages.log("Jar finished")
                    }
                    ":run"->{
                        state.value = State.NONE
                        service?.editor?.toolbar?.deactivateRun()
                        service?.editor?.toolbar?.deactivateStop()
                    }
                }
            }
            if(event is DefaultSingleProblemEvent) {
                /*
                We have 6 lines to display the error in the editor.
                 */

                if(event.definition.severity == Severity.ADVICE) return@ProgressListener
                problems.add(event)

                // TODO: Show the error on the location if it is available

                val error = event.definition.id.displayName
                service?.editor?.statusError(error)
                System.err.println("Problem: $error")

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