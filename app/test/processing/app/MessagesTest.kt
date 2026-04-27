package processing.app

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.*

class MessagesTest {
    @Test
    fun showMessage() {
        mockStatic(Base::class.java).use {
            mocked -> mocked.`when`<Boolean> { Base.isCommandLine() }.thenReturn(true)

            val streamOut = ByteArrayOutputStream()
            System.setOut(PrintStream(streamOut))

            Messages.showMessage("TestTitle", "Hello World!")
            val testOutput = streamOut.toString()

            assertTrue(testOutput.contains("TestTitle: Hello World!"))
        }
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
    fun testLog1() {
    }

    @Test
    fun testLog2() {
    }

    @Test
    fun testLogF() {
    }

    @Test
    fun err() {
    }

    @Test
    fun testErr() {
    }

}