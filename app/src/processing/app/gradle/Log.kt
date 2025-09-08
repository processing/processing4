package processing.app.gradle

import processing.app.Messages
import java.io.PrintStream
import java.net.ServerSocket

class Log{
    companion object{
        fun startLogServer(port: Int, target: PrintStream){
            val server = ServerSocket(port)
            Messages.Companion.log("Log server started on port $port")
            val client = server.accept()
            Messages.Companion.log("Log server client connected")

            val reader = client.getInputStream().bufferedReader()
            try {
                reader.forEachLine { line ->
                    if (line.isNotBlank()) {
                        target.println(line)
                    }
                }
            } catch (e: Exception) {
                Messages.Companion.log("Error while reading from log server: ${e.message}")
            } finally {
                client.close()
                server.close()
            }
        }
    }
}
