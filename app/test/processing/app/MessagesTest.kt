package processing.app

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.*

class MessagesTest {
    @Test
    fun showMessage() {
    // Mock output state; instead of output to terminal -> output to a data stream (byte array)
        val streamOut = ByteArrayOutputStream()
        System.setOut(PrintStream(streamOut))

        Messages.showMessage("TestTile: ", "Hello World!")
        val testOutput

        assertEquals()
    }

    @Test
    fun showWarning() {

    }

    @Test
    fun testShowWarning() {
    }

    @Test
    fun testShowWarning1() {
    }

    @Test
    fun showWarningTiered() {
    }

    @Test
    fun showError() {
    }

    @Test
    fun showTrace() {
    }

    @Test
    fun showYesNoQuestion() {
    }

    @Test
    fun showCustomQuestion() {
    }

    @Test
    fun log() {
    }

    @Test
    fun testLog() {
    }

    @Test
    fun logf() {
    }

    @Test
    fun err() {
    }

    @Test
    fun testErr() {
    }

}