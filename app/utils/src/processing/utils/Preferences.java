/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2014-19 The Processing Foundation

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.utils;


import processing.core.PApplet;
import processing.core.PConstants;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage class for user preferences and environment settings.
 * <P>
 * This class does not use the Properties class because .properties files use
 * ISO 8859-1 encoding, which is highly likely to be a problem when trying to
 * save sketch folders and locations. Like the rest of Processing, we use UTF8.
 * <p>
 * We don't use the Java Preferences API because it would entail writing to
 * the registry (on Windows), or an obscure file location (on Mac OS X) and
 * make it far more difficult (impossible) to remove the preferences.txt to
 * reset them (when they become corrupt), or to find the the file to make
 * edits for numerous obscure preferences that are not part of the preferences
 * window. If we added a generic editor (e.g. about:config in Mozilla) for
 * such things, we could start using the Java Preferences API. But wow, that
 * sounds like a lot of work. Not unlike writing this paragraph.
 */
public class Preferences {
    // had to rename the defaults file because people were editing it
    static final String DEFAULTS_FILE = "defaults.txt"; //$NON-NLS-1$
    static final String PREFS_FILE = "preferences.txt"; //$NON-NLS-1$

    static Map<String, String> defaults;
    static Map<String, String> table = new HashMap<>();
    static File preferencesFile;
    static File preferencesFolder;
    protected static boolean initialized = false;

    static public void init() throws PreferencesException {
        initialized = true;

        // start by loading the defaults, in case something
        // important was deleted from the user prefs
        Preferences.loadDefaults();

        // next load user preferences file
        Preferences.loadPreferences();

        boolean firstRun = !preferencesFile.exists();

        if (checkSketchbookPref() || firstRun) {
            //    if (firstRun) {
            // create a new preferences file if none exists
            // saves the defaults out to the file
            save();
        }
    }


    // load defaults.txt using JAR resource system
    static protected void loadDefaults() throws PreferencesException {
        try {
            // Name changed for 2.1b2 to avoid problems with users modifying or
            // replacing the file after doing a search for "preferences.txt".
            load(Base.getFileContent(DEFAULTS_FILE));
        } catch (Exception e) {
            throw new PreferencesException(null,
                    "Could not read default settings.\n" +
                    "You'll need to reinstall Processing.", true);
        }

        // Clone the defaults, then override any them with the user's preferences.
        // This ensures that any new/added preference will be present.
        defaults = new HashMap<>(table);
    }


    static protected void loadPreferences() throws PreferencesException {
        preferencesFile = Base.getSettingsFile(preferencesFolder, PREFS_FILE);
        boolean firstRun = !preferencesFile.exists();
        if (!firstRun) {
            try {
                load(new FileInputStream(preferencesFile));

            } catch (Exception ex) {
                throw new PreferencesException("Error reading preferences",
                                "Error reading the preferences file. " +
                                "Please delete (or move)\n" +
                                preferencesFile.getAbsolutePath() +
                                " and restart Processing.", true);
            }
        }
    }


    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


    /**
     * For testing, pretend to load preferences without a real file.
     */
    static public void skipInit() {
        initialized = true;
    }


    static public String getPreferencesPath() {
        return preferencesFile.getAbsolutePath();
    }

    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


    /**
     * Load a set of key/value pairs from a UTF-8 encoded file into 'table'.
     * For 3.0a6, this removes any platform-specific extensions from keys, so
     * that we don't have platform-specific entries in a user's preferences.txt
     * file, which would require all prefs to be changed twice, or risk being
     * overwritten by the unchanged platform-specific version on reload.
     */
    static public void load(InputStream input) throws IOException {
        HashMap<String, String> platformSpecific = new HashMap<>();

        String[] lines = PApplet.loadStrings(input);  // Reads as UTF-8
        for (String line : lines) {
            if ((line.length() == 0) ||
                    (line.charAt(0) == '#')) continue;

            // this won't properly handle = signs being in the text
            int equals = line.indexOf('=');
            if (equals != -1) {
                String key = line.substring(0, equals).trim();
                String value = line.substring(equals + 1).trim();
                if (!isPlatformSpecific(key, value, platformSpecific)) {
                    table.put(key, value);
                }
            }
        }
        // Now override the keys with any platform-specific defaults we've found.
        for (String key : platformSpecific.keySet()) {
            table.put(key, platformSpecific.get(key));
        }
    }


    /**
     * @param key original key (may include platform extension)
     * @param value the value that goes with the key
     * @param specific where to put the key/value pairs for *this* platform
     * @return true if a platform-specific key
     */
    static protected boolean isPlatformSpecific(String key, String value,
                                                Map<String, String> specific) {
        for (String platform : PConstants.platformNames) {
            String ext = "." + platform;
            if (key.endsWith(ext)) {
                String thisPlatform = PConstants.platformNames[PApplet.platform];
                if (platform.equals(thisPlatform)) {
                    key = key.substring(0, key.lastIndexOf(ext));
                    // store this for later overrides
                    specific.put(key, value);
                    //} else {
                    // ignore platform-specific defaults for other platforms,
                    // but return 'true' because it needn't be added to the big list
                }
                return true;
            }
        }
        return false;
    }


    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


    static public void save() throws PreferencesException {
        // On startup, this is null, but ignore it. It's trying to update the
        // prefs for the open sketch before Preferences.init() has been called.
        if (preferencesFile != null) {
            try {
                File dir = preferencesFile.getParentFile();
                File preferencesTemp = File.createTempFile("preferences", ".txt", dir);
                if (!preferencesTemp.setWritable(true, false)) {
                    throw new IOException("Could not set " + preferencesTemp + " writable");
                }

                // Fix for 0163 to properly use Unicode when writing preferences.txt
                PrintWriter writer = PApplet.createWriter(preferencesTemp);

                String[] keyList = table.keySet().toArray(new String[0]);
                // Sorting is really helpful for debugging, diffing, and finding keys
                keyList = PApplet.sort(keyList);
                for (String key : keyList) {
                    writer.println(key + "=" + table.get(key)); //$NON-NLS-1$
                }
                writer.flush();
                writer.close();

                // Rename preferences.txt to preferences.old
                File oldPreferences = new File(dir, "preferences.old");
                if (oldPreferences.exists()) {
                    if (!oldPreferences.delete()) {
                        throw new IOException("Could not delete preferences.old");
                    }
                }
                if (preferencesFile.exists() &&
                        !preferencesFile.renameTo(oldPreferences)) {
                    throw new IOException("Could not replace preferences.old");
                }
                // Make the temporary file into the real preferences
                if (!preferencesTemp.renameTo(preferencesFile)) {
                    throw new IOException("Could not move preferences file into place");
                }

            } catch (IOException e) {
                throw new PreferencesException("Preferences",
                        "Could not save the Preferences file.", false);
            }
        }
    }


    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


    // all the information from preferences.txt

    static public String get(String attribute /*, String defaultValue */) {
        if (!initialized) {
            throw new RuntimeException(
                    "Tried reading preferences prior to initialization."
            );
        }
        return table.get(attribute);
    }


    static public String getDefault(String attribute) {
        return defaults.get(attribute);
    }


    static public void set(String attribute, String value) {
        table.put(attribute, value);
    }


    static public void unset(String attribute) {
        table.remove(attribute);
    }


    static public boolean getBoolean(String attribute) {
        String value = get(attribute); //, null);
        return Boolean.parseBoolean(value);

    /*
      supposedly not needed, because anything besides 'true'
      (ignoring case) will just be false.. so if malformed -> false
    if (value == null) return defaultValue;

    try {
      return (new Boolean(value)).booleanValue();
    } catch (NumberFormatException e) {
      System.err.println("expecting an integer: " + attribute + " = " + value);
    }
    return defaultValue;
    */
    }


    static public void setBoolean(String attribute, boolean value) {
        set(attribute, value ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    static public int getInteger(String attribute /*, int defaultValue*/) {
        return Integer.parseInt(get(attribute));
    }


    static public void setInteger(String key, int value) {
        set(key, String.valueOf(value));
    }

    public static File getPreferencesFolder() {
        return preferencesFolder;
    }

    public static void setPreferencesFolder(File preferencesFolder) {
        Preferences.preferencesFolder = preferencesFolder;
    }

    public static File getPreferencesFile() {
        return preferencesFile;
    }


    // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


    /**
     * Check for a 4.0 sketchbook location, and if none exists,
     * try to grab it from the 3.0 sketchbook location.
     * @return true if a location was found and the pref didn't exist
     */
    static protected boolean checkSketchbookPref() {
        // If a 4.0 sketchbook location has never been inited
        if (getSketchbookPath() == null) {
            String threePath = get("sketchbook.path.three"); //$NON-NLS-1$
            // If they've run the 3.0 version, start with that location
            if (threePath != null) {
                setSketchbookPath(threePath);
                return true;  // save the sketchbook right away
            }
            // Otherwise it'll be null, and reset properly by Base
        }
        return false;
    }


    static public String getOldSketchbookPath() {
        return get("sketchbook.path.three"); //$NON-NLS-1$
    }


    static public String getSketchbookPath() {
        return get("sketchbook.path.four"); //$NON-NLS-1$
    }


    static public void setSketchbookPath(String path) {
        set("sketchbook.path.four", path); //$NON-NLS-1$
    }
}
