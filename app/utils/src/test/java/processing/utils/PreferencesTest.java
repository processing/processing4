package processing.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreferencesTest {

    @TempDir
    File settingsFolder;

    @BeforeEach
    public void clearState() {
        Preferences.reset();
    }

    @AfterEach
    public void clearOverride() {
        System.clearProperty("processing.app.preferences.file");
        Preferences.reset();
    }


    static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * The bundled defaults load from the classpath, and on a first run the
     * table starts out identical to the defaults.
     */
    @Test
    public void testInitLoadsBundledDefaults() throws IOException {
        Preferences.init(settingsFolder);
        assertNotNull(Preferences.get("editor.tabs.size"));
        assertEquals(Preferences.getDefault("editor.tabs.size"),
                     Preferences.get("editor.tabs.size"));
        assertTrue(Preferences.isInitialized());
    }


    /**
     * A first run writes preferences.txt out to the settings folder.
     */
    @Test
    public void testFirstRunCreatesPreferencesFile() throws IOException {
        var file = new File(settingsFolder, "preferences.txt");
        assertFalse(file.exists());
        Preferences.init(settingsFolder);
        assertTrue(file.exists());
    }


    /**
     * Values set and saved come back after a fresh init.
     */
    @Test
    public void testSavedValuesSurviveReload() throws IOException {
        Preferences.init(settingsFolder);
        Preferences.set("editor.tabs.size", "7");
        Preferences.save();

        Preferences.reset();
        Preferences.init(settingsFolder);
        assertEquals("7", Preferences.get("editor.tabs.size"));
    }


    /**
     * An existing preferences.txt overrides the bundled defaults,
     * but the defaults remain available via getDefault().
     */
    @Test
    public void testUserPrefsOverrideDefaults() throws IOException {
        var file = new File(settingsFolder, "preferences.txt");
        Files.writeString(file.toPath(), "editor.tabs.size=9\n");

        Preferences.init(settingsFolder);
        assertEquals("9", Preferences.get("editor.tabs.size"));
        assertEquals("2", Preferences.getDefault("editor.tabs.size"));
    }


    /**
     * The processing.app.preferences.file system property wins over the
     * settings folder passed in.
     */
    @Test
    public void testSystemPropertyOverrideWins(@TempDir File otherFolder)
            throws IOException {
        var override = new File(otherFolder, "prefs-override.txt");
        Files.writeString(override.toPath(), "override.marker=yes\n");
        System.setProperty("processing.app.preferences.file",
                           override.getAbsolutePath());

        Preferences.init(settingsFolder);
        assertEquals("yes", Preferences.get("override.marker"));
        assertEquals(override.getAbsolutePath(),
                     Preferences.getPreferencesPath());
    }


    /**
     * Keys with a suffix for this platform apply with the suffix stripped;
     * keys for other platforms are ignored entirely.
     */
    @Test
    public void testPlatformSpecificKeys() throws IOException {
        Preferences.skipInit();
        Preferences.load(stream(
            "key.windows=w\nkey.macos=m\nkey.linux=l\nkey.other=o\n"));

        var expected = switch (Platform.getName()) {
            case "windows" -> "w";
            case "macos" -> "m";
            case "linux" -> "l";
            default -> "o";
        };
        assertEquals(expected, Preferences.get("key"));
        assertNull(Preferences.get("key." + Platform.getName()));
    }


    /**
     * Backslashes in values are normalized to forward slashes on load.
     */
    @Test
    public void testBackslashesNormalized() throws IOException {
        Preferences.skipInit();
        Preferences.load(stream("sketchbook.path.four=C:\\Users\\test\n"));
        assertEquals("C:/Users/test", Preferences.get("sketchbook.path.four"));
    }


    /**
     * Comment lines and blank lines don't produce entries.
     */
    @Test
    public void testCommentsAndBlanksIgnored() throws IOException {
        Preferences.skipInit();
        Preferences.load(stream("# a comment\n\nkey = value\n"));
        assertEquals("value", Preferences.get("key"));
        assertNull(Preferences.get("# a comment"));
    }


    /**
     * getInteger() falls back to the default when the stored value
     * doesn't parse.
     */
    @Test
    public void testGetIntegerFallsBackToDefault() throws IOException {
        Preferences.init(settingsFolder);
        Preferences.set("editor.tabs.size", "not-a-number");
        assertEquals(2, Preferences.getInteger("editor.tabs.size"));
    }


    /**
     * An unreadable preferences.txt makes init throw instead of
     * silently continuing.
     */
    @Test
    public void testUnreadablePreferencesFileThrows() throws IOException {
        // a directory where the file should be cannot be opened for reading
        var blocked = new File(settingsFolder, "preferences.txt");
        assertTrue(blocked.mkdir());
        assertThrows(IOException.class, () -> Preferences.init(settingsFolder));
    }


    /**
     * preferences.txt is written with its keys sorted.
     */
    @Test
    public void testSaveWritesSortedKeys() throws IOException {
        Preferences.init(settingsFolder);
        Preferences.set("zzz.last", "1");
        Preferences.set("aaa.first", "1");
        Preferences.save();

        var lines = Files.readAllLines(
            new File(settingsFolder, "preferences.txt").toPath());
        // sorted by key, not by line: "a.b=…" sorts after "a=…" as a line,
        // but the keys "a" < "a.b" are what save() orders by
        var keys = new ArrayList<String>();
        for (var line : lines) {
            keys.add(line.substring(0, line.indexOf('=')));
        }
        var sorted = new ArrayList<>(keys);
        sorted.sort(null);
        assertEquals(sorted, keys);
    }


    /**
     * A 3.x sketchbook location is migrated to the 4.0 key on init,
     * and the migrated preference is written back out right away.
     */
    @Test
    public void testSketchbookPathMigration() throws IOException {
        var file = new File(settingsFolder, "preferences.txt");
        Files.writeString(file.toPath(),
            "sketchbook.path.three=/old/sketchbook\n");

        Preferences.init(settingsFolder);
        assertEquals("/old/sketchbook", Preferences.getSketchbookPath());
        assertEquals("/old/sketchbook", Preferences.getOldSketchbookPath());

        var saved = Files.readString(file.toPath());
        assertTrue(saved.contains("sketchbook.path.four=/old/sketchbook"));
    }


    /**
     * An existing 4.0 sketchbook location is left alone, even when a
     * 3.x location is also present.
     */
    @Test
    public void testExistingSketchbookPathNotOverwritten() throws IOException {
        var file = new File(settingsFolder, "preferences.txt");
        Files.writeString(file.toPath(),
            "sketchbook.path.three=/old\nsketchbook.path.four=/new\n");

        Preferences.init(settingsFolder);
        assertEquals("/new", Preferences.getSketchbookPath());
    }


    /**
     * Listeners hear set() and unset(), but not writes of the same value.
     */
    @Test
    public void testListenerSemantics() {
        Preferences.skipInit();
        List<String> events = new ArrayList<>();
        Preferences.ChangeListener listener =
            (key, value) -> events.add(key + "=" + value);
        Preferences.addChangeListener(listener);

        Preferences.set("a", "1");
        Preferences.set("a", "1");  // unchanged, no event
        Preferences.set("a", "2");
        Preferences.unset("a");
        Preferences.unset("a");     // already gone, no event
        assertEquals(List.of("a=1", "a=2", "a=null"), events);

        Preferences.removeChangeListener(listener);
        Preferences.set("a", "3");
        assertEquals(3, events.size());
    }


    /**
     * Bulk loading does not fire change events.
     */
    @Test
    public void testLoadIsSilentForListeners() throws IOException {
        Preferences.skipInit();
        List<String> events = new ArrayList<>();
        Preferences.addChangeListener((key, value) -> events.add(key));

        Preferences.load(stream("a=1\nb=2\n"));
        assertTrue(events.isEmpty());
    }


    /**
     * A listener that throws doesn't block the change or other listeners.
     */
    @Test
    public void testListenerExceptionContained() {
        Preferences.skipInit();
        List<String> events = new ArrayList<>();
        Preferences.addChangeListener((key, value) -> {
            throw new RuntimeException("broken listener");
        });
        Preferences.addChangeListener((key, value) -> events.add(key));

        Preferences.set("a", "1");
        assertEquals("1", Preferences.get("a"));
        assertEquals(List.of("a"), events);
    }
}
