package processing.utils;

import processing.utils.settingslocation.*;

import java.io.File;

public class SettingsResolver {
    /**
     * Get the directory that can store settings. (Library on OS X, App Data or
     * something similar on Windows, a dot folder on Linux.) Removed this as a
     * preference for 3.0a3 because we need this to be stable, but adding back
     * for 4.0 beta 4 so that folks can do 'portable' versions again.
     */
    static public File getSettingsFolder() throws Exception {
        File settingsFolder = null;

        String os = System.getProperty("os.name");
        DefaultLocation loc = null;

        if (os.contains("Mac")) {
            loc = new MacLocation();
        } else if (os.contains("Linux")) {
            loc = new LinuxLocation();
        } else if (os.contains("Windows")) {
            loc = new WindowsLocation();
        } else {
            loc = new DefaultLocation();
        }

        settingsFolder = loc.getSettingsFolder();

        return settingsFolder;

    }
}
