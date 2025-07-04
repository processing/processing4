package processing.utils.settingslocation;

import java.io.File;
import java.io.FileNotFoundException;

public class MacLocation extends DefaultLocation{

    public File getSettingsFolder() throws Exception {
        return new File(getLibraryFolder(), "Processing");
    }


    // TODO I suspect this won't work much longer, since access to the user's
    //      home directory seems verboten on more recent macOS versions [fry 191008]
    //      However, anecdotally it seems that just using the name works,
    //      and the localization is handled transparently. [fry 220116]
    //      https://github.com/processing/processing4/issues/9
    protected String getLibraryFolder() throws FileNotFoundException {
        File folder = new File(System.getProperty("user.home"), "Library");
        if (!folder.exists()) {
            throw new FileNotFoundException("Folder missing: " + folder);
        }
        return folder.getAbsolutePath();
    }
}
