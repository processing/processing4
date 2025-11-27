package processing.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;

public class Settings {
    public static File getFolder() throws SettingsFolderException {
        try {
            var folder = getFolderForPlatform();
            if (!folder.exists() && !folder.mkdirs()) {
                throw new SettingsFolderException(SettingsFolderException.Type.COULD_NOT_CREATE_FOLDER, folder.getAbsolutePath());
            }
            return folder;
        } catch (RuntimeException e) {
            throw new SettingsFolderException(SettingsFolderException.Type.UNKNOWN);
        }
    }

    private static File getFolderForPlatform() throws SettingsFolderException {
        var settingsOverride = System.getProperty("processing.settings.folder");
        if (settingsOverride != null && !settingsOverride.isEmpty()) {
            return new File(settingsOverride);
        }

        var portableSettings = FindPortableSettings();
        if (portableSettings.isPresent()) {
            return portableSettings.get();
        }

        if (Platform.isWindows()) {
            var options = new String[]{
                    "APPDATA",
                    "LOCALAPPDATA"
            };
            for (String option : options) {
                var folder = new File(System.getenv(option), "Processing");
                if (!folder.exists() && !folder.mkdirs()) {
                    continue;
                }
                return folder;
            }
            throw new SettingsFolderException(SettingsFolderException.Type.WINDOWS_APPDATA_NOT_FOUND);
        }
        if (Platform.isMacOS()) {
            var folder = new File(System.getProperty("user.home"), "Library");
            if (!folder.exists()) {
                throw new SettingsFolderException(SettingsFolderException.Type.MACOS_LIBRARY_FOLDER_NOT_FOUND);
            }
            return new File(folder, "Processing");
        }
        if (Platform.isLinux()) {
            var options = new String[]{
                    "SNAP_USER_COMMON",
                    "XDG_CONFIG_HOME"
            };
            for (String option : options) {
                var configHomeEnv = System.getenv(option);
                if (configHomeEnv == null || configHomeEnv.isBlank()) {
                    continue;
                }
                var parentFolder = new File(configHomeEnv);
                if (!parentFolder.exists()) {
                    throw new SettingsFolderException(SettingsFolderException.Type.LINUX_CONFIG_FOLDER_NOT_FOUND);
                }
                var folder = new File(parentFolder, "processing");
                if (!folder.exists() && !folder.mkdirs()) {
                    continue;
                }
                return folder;
            }
            var subfolder = "/.config/processing";
            var isSudo = System.getenv("SUDO_USER");
            if (isSudo == null || isSudo.isEmpty()) {
                return new File(System.getProperty("user.home") + subfolder);
            }
            // If user is SUDO_USER, try to get their home directory
            try {
                var process = Runtime.getRuntime().exec(
                        new String[]{
                                "/bin/sh", "-c", "echo ~" + isSudo
                        }
                );
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    return new File(reader.readLine() + subfolder);
                }
            } catch (Exception e) {
                throw new SettingsFolderException(SettingsFolderException.Type.LINUX_SUDO_USER_ERROR);
            }
        }

        // If all else fails, use ~/.processing
        return new File(System.getProperty("user.home"), ".processing");
    }

    /**
     * find a preferences.txt file in the same folder as the running jar/executable
     *
     * @return Optional File pointing to preferences.txt if found, empty otherwise
     */
    private static Optional<File> FindPortableSettings() {
        var command = ProcessHandle.current().info().command();
        if (command.isEmpty()) return Optional.empty();

        var path = command.get();
        path = path.replaceAll("/[^/]+$", "");

        if (Platform.isMacOS()) {
            // On macOS, the executable is inside the .app bundle, so we need to go up to above the .app folder
            path = path.replaceAll("/[^/]+\\.app/.*$", "");
        }
        var file = new File(path, "preferences.txt");
        if (System.getenv().containsKey("DEBUG"))
            System.out.println("Looking for portable settings at: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            return Optional.empty();
        }
        return Optional.of(new File(path));

    }

    public static class SettingsFolderException extends Exception {
        public enum Type {
            COULD_NOT_CREATE_FOLDER,
            WINDOWS_APPDATA_NOT_FOUND,
            MACOS_LIBRARY_FOLDER_NOT_FOUND,
            LINUX_CONFIG_FOLDER_NOT_FOUND,
            LINUX_SUDO_USER_ERROR,
            UNKNOWN
        }

        private final Type type;

        public SettingsFolderException(Type type) {
            this.type = type;
        }

        public SettingsFolderException(Type type, String message) {
            super(message);
            this.type = type;
        }

        public Type getType() {
            return type;
        }
    }
}
