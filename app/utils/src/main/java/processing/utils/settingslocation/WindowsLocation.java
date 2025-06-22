package processing.utils.settingslocation;

import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;
import processing.utils.Messages;
import processing.utils.Util;

import java.io.File;
import java.io.IOException;

public class WindowsLocation extends DefaultLocation{
    static final String APP_NAME = "Processing";

    // looking for Documents and Settings/blah/Application Data/Processing
    public File getSettingsFolder() throws Exception {

        try {
            String appDataRoaming = getAppDataPath();
            if (appDataRoaming != null) {
                File settingsFolder = new File(appDataRoaming, APP_NAME);
                if (settingsFolder.exists() || settingsFolder.mkdirs()) {
                    return settingsFolder;
                }
            }

            String appDataLocal = getLocalAppDataPath();
            if (appDataLocal != null) {
                File settingsFolder = new File(appDataLocal, APP_NAME);
                if (settingsFolder.exists() || settingsFolder.mkdirs()) {
                    return settingsFolder;
                }
            }

            if (appDataRoaming == null && appDataLocal == null) {
                throw new IOException("Could not get the AppData folder");
            }

            // https://github.com/processing/processing/issues/3838
            throw new IOException("Permissions error: make sure that " +
                    appDataRoaming + " or " + appDataLocal +
                    " is writable.");

        } catch (UnsatisfiedLinkError ule) {
            String path = new File("lib").getCanonicalPath();

            String msg = Util.containsNonASCII(path) ?
                    """
                      Please move Processing to a location with only
                      ASCII characters in the path and try again.
                      https://github.com/processing/processing/issues/3543
                    """ :
                    "Could not find JNA support files, please reinstall Processing.";
            Messages.showError("Windows JNA Problem", msg, ule);
            return null;  // unreachable
        }
    }


    /** Get the Users\name\AppData\Roaming path to write settings files. */
    static private String getAppDataPath() {
        return Shell32Util.getSpecialFolderPath(ShlObj.CSIDL_APPDATA, true);
    }


    /** Get the Users\name\AppData\Local path as a settings fallback. */
    static private String getLocalAppDataPath() {
        return Shell32Util.getSpecialFolderPath(ShlObj.CSIDL_LOCAL_APPDATA, true);
    }
}
