package processing.app.gradle

import com.sun.jdi.Bootstrap
import com.sun.jdi.VirtualMachine
import com.sun.jdi.connect.AttachingConnector
import kotlinx.coroutines.delay
import processing.app.Messages
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class Debugger {
    companion object {
        suspend fun connect(port: Int?): VirtualMachine? {
            try {
                Messages.log("Attaching to VM $port")
                val connector = Bootstrap.virtualMachineManager().allConnectors()
                    .firstOrNull { it.name() == "com.sun.jdi.SocketAttach" }
                        as AttachingConnector?
                    ?: throw IllegalStateException("No socket attach connector found")
                val args = connector.defaultArguments()
                args["port"]?.setValue(port?.toString() ?: "5005")

                // Try to attach the debugger, retrying if it fails
                val start = TimeSource.Monotonic.markNow()
                while (start.elapsedNow() < 10.seconds) {
                    try {
                        val sketch = connector.attach(args)
                        sketch.resume()
                        Messages.log("Attached to VM: ${sketch.name()}")
                        return sketch
                    } catch (e: Exception) {
                        Messages.log("Error while attaching to VM: ${e.message}... Retrying")
                    }
                    delay(250)
                }
            } catch (e: Exception) {
                Messages.log("Error while attaching to VM: ${e.message}")
                return null
            }
            return null
        }
    }
}