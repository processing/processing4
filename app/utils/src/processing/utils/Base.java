package processing.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Base {

    // load file content using JAR resource system
    static public InputStream getFileContent(String fileName) throws IOException {
        InputStream inputStream = Base.class.getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IOException("defaults.txt not found in resources");
        }

        return inputStream;
    }

    static public File getSettingsFile(File settingsFolder, String fileName){
        return new File(settingsFolder, fileName);
    }

}
