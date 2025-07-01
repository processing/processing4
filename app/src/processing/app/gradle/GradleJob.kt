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
import processing.app.Base
import processing.app.Messages
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.lang.IllegalStateException

// TODO: Move the error reporting to its own file
// TODO: Move the output filtering to its own file
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

    private val outputStream = PipedOutputStream()
    private val errorStream = PipedOutputStream()

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
                        setStandardOutput(outputStream)
                        setStandardError(errorStream)
                        run()
                    }
            }catch (e: Exception){
                Messages.log("Error while running: ${e.message} ${e.cause?.message}")
            }finally {
                state.value = State.DONE
                vm.value = null
            }
        }
        // TODO: I'm sure this can be done better
        scope.launch {
            try {
                InputStreamReader(PipedInputStream(outputStream)).buffered().use { reader ->
                    reader.lineSequence()
                        .forEach { line ->
                            if (cancel.token().isCancellationRequested) {
                                return@launch
                            }
                            if (state.value != State.RUNNING) {
                                return@forEach
                            }
                            service?.out?.println(line)
                        }
                }
            }catch (e: Exception){
                Messages.log("Error while reading output: ${e.message}")
            }
        }
        scope.launch {
            try {
                InputStreamReader(PipedInputStream(errorStream)).buffered().use { reader ->
                    reader.lineSequence()
                        .forEach { line ->
                            if (cancel.token().isCancellationRequested) {
                                return@launch
                            }
                            if (state.value != State.RUNNING) {
                                return@forEach
                            }
                            when{
                                line.contains("+[IMKClient subclass]: chose IMKClient_Modern") -> return@forEach
                                line.contains("+[IMKInputSession subclass]: chose IMKInputSession_Modern") -> return@forEach
                                line.startsWith("__MOVE__") -> return@forEach
                                else -> service?.err?.println(line)
                            }
                        }
                }
            }catch (e: Exception){
                Messages.log("Error while reading error: ${e.message}")
            }
        }

    }

    fun cancel(){
        cancel.cancel()
    }
    private fun BuildLauncher.addStateListener(){
        addProgressListener(ProgressListener { event ->
            if(event is TaskStartEvent) {
                when(event.descriptor.name) {
                    ":run" -> {
                        state.value = State.RUNNING
                        Messages.log("Start run")
                    }
                }

            }
            if(event is TaskFinishEvent) {
                when(event.descriptor.name){
                    ":jar"->{
                        state.value = State.NONE
                        Messages.log("Jar finished")
                    }
                    ":run"->{
                        state.value = State.NONE
                    }
                }
            }
            if(event is DefaultSingleProblemEvent) {
                // TODO: Move to UI instead of printing
                if(event.definition.severity == Severity.ADVICE) return@ProgressListener
                problems.add(event)

                val path = (event.locations.firstOrNull() as DefaultFileLocation?)?.path

                val header = """
                    ${event.definition.id.displayName}: 
                        ${event.contextualLabel.contextualLabel}
                    """.trimIndent()

                val details = event.details.details?.replace(path ?: "", "")
                val solutions = event.solutions.joinToString("\n") { it.solution }
                val content = "$header\n$details\n$solutions"
                service?.err?.println(content)
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
                Exceptions.listen(debugger)
            }

        })
    }
}