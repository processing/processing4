package processing.utils.settingslocation;

import processing.core.PApplet;
import processing.utils.Messages;

import java.io.File;

public class LinuxLocation extends DefaultLocation{
    String homeDir;

    @Override
    public File getSettingsFolder() throws Exception {
        // https://github.com/processing/processing4/issues/203
        // https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html

        File configHome = null;

        // Check to see if the user has set a different location for their config
        String configHomeEnv = System.getenv("XDG_CONFIG_HOME");
        if (configHomeEnv != null && !configHomeEnv.isBlank()) {
            configHome = new File(configHomeEnv);
            if (!configHome.exists()) {
                Messages.err("XDG_CONFIG_HOME is set to " + configHomeEnv + " but does not exist.");
                configHome = null;  // don't use non-existent folder
            }
        }
        String snapUserCommon = System.getenv("SNAP_USER_COMMON");
        if (snapUserCommon != null && !snapUserCommon.isBlank()) {
            configHome = new File(snapUserCommon);
        }
        // If not set properly, use the default
        if (configHome == null) {
            configHome = new File(getHomeDir(), ".config");
        }
        return new File(configHome, "processing");
    }


    // Java sets user.home to be /root for execution with sudo.
    // This method attempts to use the user's real home directory instead.
    public String getHomeDir() {
        if (homeDir == null) {
            // get home directory of SUDO_USER if set, else use user.home
            homeDir = System.getProperty("user.home");
            String sudoUser = System.getenv("SUDO_USER");
            if (sudoUser != null && sudoUser.length() != 0) {
                try {
                    homeDir = getHomeDir(sudoUser);
                } catch (Exception ignored) { }
            }
        }
        return homeDir;
    }


    static public String getHomeDir(String user) throws Exception {
        Process p = PApplet.exec("/bin/sh", "-c", "echo ~" + user);
        return PApplet.createReader(p.getInputStream()).readLine();
    }
}
