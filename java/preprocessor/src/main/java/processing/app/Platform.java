package processing.app;

import java.io.File;

public class Platform {
    static public File getSettingsFolder() {
        File settingsFolder = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            settingsFolder = new File(System.getProperty("user.home") + "/Library/Processing");
        } else if (os.contains("windows")) {
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = System.getProperty("user.home");
            }
            settingsFolder = new File(appData + "\\Processing");
        } else {
            // Check to see if the user has set a different location for their config
            String configHomeEnv = System.getenv("XDG_CONFIG_HOME");
            if (configHomeEnv != null && !configHomeEnv.isBlank()) {
                settingsFolder = new File(configHomeEnv);
                if (!settingsFolder.exists()) {
                    settingsFolder = null;  // don't use non-existent folder
                }
            }
            String snapUserCommon = System.getenv("SNAP_USER_COMMON");
            if (snapUserCommon != null && !snapUserCommon.isBlank()) {
                settingsFolder = new File(snapUserCommon);
            }
            if (settingsFolder == null) {
                settingsFolder = new File(System.getProperty("user.home"), ".config");
            }
        }
        return settingsFolder;
    }
}
