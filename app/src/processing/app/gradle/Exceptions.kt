package processing.app.gradle

import com.sun.jdi.ObjectReference
import com.sun.jdi.StackFrame
import com.sun.jdi.StringReference
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.ExceptionEvent
import com.sun.jdi.request.EventRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import processing.app.Messages

// TODO: Consider adding a panel to the footer
class Exceptions {
    companion object {
        suspend fun listen(vm: VirtualMachine) {
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
            val location = event.location()
            val stackFrames = thread.frames()

            println("\n🚨 Exception Caught 🚨")
            println("Type       : ${exception.referenceType().name()}")
            // TODO: Fix exception message retrieval
//        println("Message    : ${getExceptionMessage(exception)}")
            println("Thread     : ${thread.name()}")
            println("Location   : ${location.sourcePath()}:${location.lineNumber()}\n")

            // TODO: Map to .pde file again
            // TODO: Communicate back to Editor

            // Separate stack frames
            val userFrames = mutableListOf<StackFrame>()
            val processingFrames = mutableListOf<StackFrame>()

            stackFrames.forEach { frame ->
                val className = frame.location().declaringType().name()
                if (className.startsWith("processing.")) {
                    processingFrames.add(frame)
                } else {
                    userFrames.add(frame)
                }
            }

            // Print user frames first
            println("🔍 Stacktrace (Your Code First):")
            userFrames.forEachIndexed { index, frame -> printStackFrame(index, frame) }

            // Print Processing frames second
            if (processingFrames.isNotEmpty()) {
                println("\n🔧 Processing Stacktrace (Hidden Initially):")
                processingFrames.forEachIndexed { index, frame -> printStackFrame(index, frame) }
            }

            println("──────────────────────────────────\n")
        }

        fun printStackFrame(index: Int, frame: StackFrame) {
            val location = frame.location()
            val method = location.method()
            println(
                "   #$index ${location.sourcePath()}:${location.lineNumber()} -> ${
                    method.declaringType().name()
                }.${method.name()}()"
            )
        }

        // Extracts the exception's message
        fun getExceptionMessage(exception: ObjectReference): String {
            val messageMethod = exception.referenceType().methodsByName("getMessage").firstOrNull() ?: return "Unknown"
            val messageValue =
                exception.invokeMethod(null, messageMethod, emptyList(), ObjectReference.INVOKE_SINGLE_THREADED)
            return (messageValue as? StringReference)?.value() ?: "Unknown"
        }
    }
}