import org.junit.jupiter.api.Test;
import processing.utils.Settings;

import java.io.IOException;
import java.nio.file.Files;

public class SettingsTest {

    /**
     * Requesting the settings folder should create it if it doesn't exist
     */
    @Test
    public void testSettingsFolder() {
        try {
            var folder = Settings.getFolder();
            assert (folder.exists());
        } catch (Settings.SettingsFolderException e) {
            assert (false);
        }
    }

    /**
     * Overriding the settings folder via system property should work
     */
    @Test
    public void testOverrideFolder() throws IOException {
        var settings = Files.createTempDirectory("settings_test");
        System.setProperty("processing.settings.folder", settings.toString());

        try {
            var folder = Settings.getFolder();
            assert (folder.toPath().toString().equals(settings.toString()));
        } catch (Settings.SettingsFolderException e) {
            assert (false);
        } finally {
            System.clearProperty("processing.settings.folder");
            Files.deleteIfExists(settings);
        }
    }
}
