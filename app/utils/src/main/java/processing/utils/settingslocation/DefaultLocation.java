package processing.utils.settingslocation;

import java.io.File;

public class DefaultLocation {
    /**
     * This function should throw an exception or return a value.
     * Do not return null.
     */
    public File getSettingsFolder() throws Exception {
        // If no subclass has a behavior, default to making a
        // ".processing" directory in the user's home directory.
        File home = new File(System.getProperty("user.home"));
        return new File(home, ".processing");
    }

}
