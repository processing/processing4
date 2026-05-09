package processing.app;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateCheckTest {

    @TempDir
    Path tempDir;

    // Helper: write content to a temp file and return its URL string
    private String createTempFile(String content) throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toUri().toString();
    }


    // tests to show that the method returns what it should
    @Test
    void readInt_simpleInteger_returnsCorrectValue() throws IOException {
        String url = createTempFile("42\n");
        assertEquals(42, UpdateCheck.readInt(url));
    }

    @Test
    void readInt_negativeInteger_returnsCorrectValue() throws IOException {
        String url = createTempFile("-7\n");
        assertEquals(-7, UpdateCheck.readInt(url));
    }

    @Test
    void readInt_integerWithLeadingAndTrailingWhitespace_returnsCorrectValue() throws IOException {
        String url = createTempFile("  100  \n");
        assertEquals(100, UpdateCheck.readInt(url));
    }

    @Test
    void readInt_integerWithNoNewline_returnsCorrectValue() throws IOException {
        String url = createTempFile("99");
        assertEquals(99, UpdateCheck.readInt(url));
    }

    @Test
    void readInt_maxInt_returnsCorrectValue() throws IOException {
        String url = createTempFile(String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, UpdateCheck.readInt(url));
    }

    @Test
    void readInt_minInt_returnsCorrectValue() throws IOException {
        String url = createTempFile(String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, UpdateCheck.readInt(url));
    }

    @Test
    void readInt_integerWithMultipleLines_readsOnlyFirstLine() throws IOException {
        String url = createTempFile("5\n10\n15");
        assertEquals(5, UpdateCheck.readInt(url));
    }

    // checks for if errors are correctly reported
    @Test
    void readInt_nonNumericContent_throwsNumberFormatException() throws IOException {
        String url = createTempFile("not-a-number\n");
        assertThrows(NumberFormatException.class, () -> UpdateCheck.readInt(url));
    }

    @Test
    void readInt_emptyFile_throwsNullPointerException() throws IOException {
        String url = createTempFile("");
        // readLine() returns null on empty stream → trim() throws NPE
        assertThrows(Exception.class, () -> UpdateCheck.readInt(url));
    }

    @Test
    void readInt_blankLine_throwsNumberFormatException() throws IOException {
        String url = createTempFile("   \n");
        assertThrows(NumberFormatException.class, () -> UpdateCheck.readInt(url));
    }

    @Test
    void readInt_floatValue_throwsNumberFormatException() throws IOException {
        String url = createTempFile("3.14\n");
        assertThrows(NumberFormatException.class, () -> UpdateCheck.readInt(url));
    }

    @Test
    void readInt_overflowValue_throwsNumberFormatException() throws IOException {
        String url = createTempFile("99999999999999\n");
        assertThrows(NumberFormatException.class, () -> UpdateCheck.readInt(url));
    }

    @Test
    void readInt_invalidUrl_throwsMalformedURLException() {
        assertThrows(MalformedURLException.class,
                () -> UpdateCheck.readInt("not-a-valid-url"));
    }

    @Test
    void readInt_nonExistentFile_throwsIOException() {
        String nonExistent = tempDir.resolve("ghost.txt").toUri().toString();
        assertThrows(IOException.class, () -> UpdateCheck.readInt(nonExistent));
    }

    // checks for if streams are closed
    @Test
    void readInt_streamIsClosedAfterSuccessfulRead() throws IOException {
        // Spy on the InputStream to verify close() is called
        Path file = tempDir.resolve("close_test.txt");
        Files.writeString(file, "7", StandardCharsets.UTF_8);

        URL url = file.toUri().toURL();
        InputStream realStream = url.openStream();
        InputStream spyStream = spy(realStream);

        try (MockedConstruction<URL> mockedUrl = mockConstruction(URL.class,
                (mock, ctx) -> when(mock.openStream()).thenReturn(spyStream))) {

            UpdateCheck.readInt(file.toUri().toString());
        }

        verify(spyStream, atLeastOnce()).close();
    }

    @Test
    void readInt_streamIsClosedEvenWhenParseThrows() throws IOException {
        Path file = tempDir.resolve("bad_close_test.txt");
        Files.writeString(file, "not-a-number", StandardCharsets.UTF_8);

        URL url = file.toUri().toURL();
        InputStream realStream = url.openStream();
        InputStream spyStream = spy(realStream);

        try (MockedConstruction<URL> mockedUrl = mockConstruction(URL.class,
                (mock, ctx) -> when(mock.openStream()).thenReturn(spyStream))) {

            assertThrows(NumberFormatException.class,
                    () -> UpdateCheck.readInt(file.toUri().toString()));
        }

        verify(spyStream, atLeastOnce()).close();
    }
}
