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
}
