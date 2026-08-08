package processing.utils;

public class Platform {
    /**
     * returns true if Processing is running on a Mac OS X machine.
     */
    static public boolean isMacOS() {
        return System.getProperty("os.name").contains("Mac"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * returns true if running on windows.
     */
    static public boolean isWindows() {
        return System.getProperty("os.name").contains("Windows"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * true if running on linux.
     */
    static public boolean isLinux() {
        return System.getProperty("os.name").contains("Linux"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    /**
     * Platform name for the current OS, mirroring core's
     * PConstants.platformNames: "windows", "macos", "linux", or "other".
     */
    static public String getName() {
        if (isWindows()) return "windows"; //$NON-NLS-1$
        if (isMacOS())   return "macos";   //$NON-NLS-1$
        if (isLinux())   return "linux";   //$NON-NLS-1$
        return "other";                    //$NON-NLS-1$
    }
}
