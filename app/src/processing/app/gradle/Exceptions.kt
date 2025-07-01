package processing.app.gradle

import com.sun.jdi.Location
import com.sun.jdi.StackFrame
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.ExceptionEvent
import com.sun.jdi.request.EventRequest
import kotlinx.coroutines.delay
import processing.app.Messages
import processing.app.SketchException
import processing.app.ui.Editor

// TODO: Consider adding a panel to the footer
class Exceptions (val vm: VirtualMachine, val editor: Editor?) {
    suspend fun listen() {
        try {
            val manager = vm.eventRequestManager()

            val request = manager.createExceptionRequest(null, false, true)
            request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD)
            request.enable()

            val queue = vm.eventQueue()
            while (true) {
                val eventSet = queue.remove()
                for (event in eventSet) {
                    if (event is ExceptionEvent) {
                        printExceptionDetails(event)
                        event.thread().resume()
                    }
                }
                eventSet.resume()
                delay(10)
            }
        } catch (e: Exception) {
            Messages.log("Error while listening for exceptions: ${e.message}")
        }
    }

    fun printExceptionDetails(event: ExceptionEvent) {
        val exception = event.exception()
        val thread = event.thread()
        val location = event.location().mapToPdeFile()
        val stackFrames = thread.frames()

        val (processingFrames, userFrames) = stackFrames
            .map{
                val location = it.location().mapToPdeFile()
                val method = location.method()
                it to "${method.declaringType().name()}.${method.name()}() @ ${location.sourcePath()}:${location.lineNumber()}"
            }
            .partition {
                it.first.location().declaringType().name().startsWith("processing.")
            }

        /*
        We have 6 lines by default within the editor to display more information about the exception.
         */

        val message = """
            In Processing code:
                #processingFrames
            
            In your code:
                #userFrames
                
        """
            .trimIndent()
            .replace("#processingFrames", processingFrames.joinToString("\n    ") { it.second })
            .replace("#userFrames", userFrames.joinToString("\n    ") { it.second })

        val error = """
            Exception: ${exception.referenceType().name()} @ ${location.sourcePath()}:${location.lineNumber()}
        """.trimIndent()

        println(message)
        System.err.println(error)

        editor?.statusError(exception.referenceType().name())
    }

    fun Location.mapToPdeFile(): Location {
        if(editor == null) return this

        // Check if the source is a .java file
        val sketch = editor.sketch
        sketch.code.forEach { code ->
            if(code.extension != "java") return@forEach
            if(sourceName() != code.fileName) return@forEach
            return@mapToPdeFile this
        }

        // TODO: Map to .pde file again, @see JavaBuild.placeException
        // BLOCKED: Because we don't run the JavaBuild code.prepocOffset is empty

        return this
    }
}