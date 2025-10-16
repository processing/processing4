package processing.core;

import java.io.*;
import java.nio.file.*;

/**
 * Handles loading of Processing's native Rust library (libprocessing).
 */
public class NativeLibrary {
    private static boolean loaded = false;
    private static Throwable loadError = null;

    private static final String LIBRARY_NAME = "processing";

    // Platform
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();

    private static final String platform;
    private static final String architecture;
    private static final String libraryExtension;

    static {
        // platform
        if (OS_NAME.contains("mac") || OS_NAME.contains("darwin")) {
            platform = "macos";
            libraryExtension = "dylib";
        } else if (OS_NAME.contains("win")) {
            platform = "windows";
            libraryExtension = "dll";
        } else if (OS_NAME.contains("linux")) {
            platform = "linux";
            libraryExtension = "so";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + OS_NAME);
        }

        // architecture
        if (OS_ARCH.contains("aarch64") || OS_ARCH.contains("arm")) {
            architecture = "aarch64";
        } else if (OS_ARCH.contains("x86_64") || OS_ARCH.contains("amd64")) {
            architecture = "x86_64";
        } else {
            throw new UnsupportedOperationException("Unsupported architecture: " + OS_ARCH);
        }

        // Load the dll
        try {
            loadNativeLibrary();
            loaded = true;
        } catch (Throwable e) {
            loadError = e;
            System.err.println("Warning: Failed to load Processing native library: " + e.getMessage());
        }
    }

    /**
     * Ensures the native library is loaded. Throws if loading failed.
     */
    public static void ensureLoaded() {
        if (!loaded) {
            throw new RuntimeException("Native library failed to load", loadError);
        }
    }

    /**
     * Returns whether the native library was successfully loaded.
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Returns the platform string (e.g., "macos-aarch64").
     */
    public static String getPlatform() {
        return platform + "-" + architecture;
    }

    /**
     * Extracts and loads the native library from JAR resources.
     */
    private static void loadNativeLibrary() throws IOException {
        String platformTarget = platform + "-" + architecture;
        String libraryFileName = "lib" + LIBRARY_NAME + "." + libraryExtension;
        String resourcePath = "/native/" + platformTarget + "/" + libraryFileName;

        // check classloader for resource in jar
        InputStream libraryStream = NativeLibrary.class.getResourceAsStream(resourcePath);
        if (libraryStream == null) {
            throw new FileNotFoundException(
                "Native library not found in JAR: " + resourcePath +
                " (platform: " + platformTarget + ")"
            );
        }

        // extract
        Path tempDir = Files.createTempDirectory("processing-native-");
        tempDir.toFile().deleteOnExit();

        Path libraryPath = tempDir.resolve(libraryFileName);
        Files.copy(libraryStream, libraryPath, StandardCopyOption.REPLACE_EXISTING);
        libraryStream.close();

        libraryPath.toFile().deleteOnExit();

        // load!
        System.load(libraryPath.toAbsolutePath().toString());
    }
}
