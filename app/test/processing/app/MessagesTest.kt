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
        val baseMock = mockStatic(Base::class.java)

        baseMock.`when`<Boolean> { Base.isCommandLine() }.thenReturn(true)

        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()

        System.setOut(PrintStream(out))
        System.setErr(PrintStream(err))

        val ex = RuntimeException("test")

        Messages.showWarning("Warning", "Something happened", ex)

        assertTrue(out.toString().contains("Warning: Something happened"))
        assertTrue(err.toString().contains("test"))

        baseMock.close()
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
            mockStatic(Base::class.java).use { baseMock ->
            baseMock.`when`<Boolean> { Base.isCommandLine() }.thenReturn(true)

            val err = ByteArrayOutputStream()
            System.setErr(PrintStream(err))

            val ex = RuntimeException("boom")

            Messages.showTrace("Title", "Something broke", ex, false)

            val output = err.toString()

            assertTrue(output.contains("Title: Something broke"))
            assertTrue(output.contains("boom"))
        }
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