package processing.utils;

import java.io.File;
import java.io.InputStream;

public class Base {
    static public boolean DEBUG = System.getenv().containsKey("DEBUG");


    static public boolean isCommandLine() {
        return Boolean.getBoolean("processing.cli");
    }


    /**
     * Convenience method to get a File object for the specified filename inside
     * the settings folder. Used to get preferences and recent sketch files.
     *
     * @param filename A file inside the settings folder.
     * @return filename wrapped as a File object inside the settings folder
     */
    static public File getSettingsFile(String filename) {
        File settingsFolder = null;

        try {
            settingsFolder = SettingsResolver.getSettingsFolder();

            // create the folder if it doesn't exist already
            if (!settingsFolder.exists()) {
                if (!settingsFolder.mkdirs()) {
                    Messages.showError("Settings issues",
                            "Could not create the folder" +
                                    settingsFolder, null);
                }
            }
        } catch (Exception e) {
            Messages.showError("An rare and unknowable thing happened",
                               "Could not get the settings folder.", e);
        }
        return new File(settingsFolder, filename);
    }


    /**
     * Retrieves an InputStream to a resource file located within the JAR package.
     * This method uses Java's resource loading system to fetch files bundled in the application,
     * such as configuration files, data, or assets.
     *
     * @param resourceName The name or path of the resource file to be loaded.
     *                     This should match the location in the JAR's structure.
     * @return An InputStream that can be used to read the contents of the requested resource file,
     *         or null if the resource is not found.
     * @throws IllegalArgumentException if the resource cannot be located.
     */
    public static InputStream getLibStream(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            throw new IllegalArgumentException("Resource name cannot be null or empty");
        }
        // Ensure the resource name starts with exactly one "/"
        if (!resourceName.startsWith("/")) {
            resourceName = "/" + resourceName; // Prepend "/" if missing
        } else {
            resourceName = resourceName.replaceAll("^/+","/"); // Ensure only one "/" at start
        }

        InputStream stream = Base.class.getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }
        return stream;
    }
}
