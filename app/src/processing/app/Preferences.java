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

package processing.app;

import processing.app.ui.Toolkit;
import processing.core.PApplet;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.io.IOException;
import java.io.InputStream;


/**
 * The app-facing side of the preferences. The actual storage (loading and
 * saving preferences.txt, the bundled defaults, platform-specific keys)
 * lives in the standalone {@link processing.utils.Preferences}; this class
 * delegates to it and keeps the pieces that only make sense inside the
 * full application: fonts and colors, error dialogs, and environment
 * side effects like proxies and the native file chooser.
 */
public class Preferences {

  static public void init() {
    // start by loading the defaults, in case something
    // important was deleted from the user prefs
    try {
      processing.utils.Preferences.loadDefaults();
    } catch (IOException e) {
      Messages.showError(null, "Could not read default settings.\n" +
                         "You'll need to reinstall Processing.", e);
    }

    // other things that have to be set explicitly for the defaults
    setColor("run.window.bgcolor", SystemColor.control); //$NON-NLS-1$

    // For CJK users, enable IM support by default
    if (Language.useInputMethod()) {
      setBoolean("editor.input_method_support", true);
    }

    // next load user preferences file over the defaults
    try {
      processing.utils.Preferences.loadUserPrefs(Base.getSettingsFolder());
    } catch (IOException ex) {
      Messages.showError("Error reading preferences",
                         "Error reading the preferences file. " +
                         "Please delete (or move)\n" +
                         processing.utils.Preferences.getPreferencesPath() +
                         " and restart Processing.", ex);
    }

    PApplet.useNativeSelect =
      Preferences.getBoolean("chooser.files.native"); //$NON-NLS-1$

    // Adding option to disable this in case it's getting in the way
    if (get("proxy.system").equals("true")) {
      // Use the system proxy settings by default
      // https://github.com/processing/processing/issues/2643
      System.setProperty("java.net.useSystemProxies", "true");
    }

    // Set HTTP, HTTPS, and SOCKS proxies for individuals
    // who want/need to override the system setting
    // http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
    // Less readable version with the Oracle style sheet:
    // http://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
    handleProxy("http", "http.proxyHost", "http.proxyPort");
    handleProxy("https", "https.proxyHost", "https.proxyPort");
    handleProxy("socks", "socksProxyHost", "socksProxyPort");
  }


  /**
   * For testing, pretend to load preferences without a real file.
   */
  static public void skipInit() {
    processing.utils.Preferences.skipInit();
  }

  /**
   * Check whether Preferences.init() has been called. If not, we are probably not running the full application.
   * @return true if Preferences has been initialized
   */
  static public boolean isInitialized() {
    return processing.utils.Preferences.isInitialized();
  }


  static void handleProxy(String protocol, String hostProp, String portProp) {
    String proxyHost = get("proxy." + protocol + ".host");
    String proxyPort = get("proxy." + protocol + ".port");
    if (proxyHost != null && proxyHost.length() != 0 &&
        proxyPort != null && proxyPort.length() != 0) {
      System.setProperty(hostProp, proxyHost);
      System.setProperty(portProp, proxyPort);
    }

  }


  static public String getPreferencesPath() {
    return processing.utils.Preferences.getPreferencesPath();
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  static public void load(InputStream input) throws IOException {
    processing.utils.Preferences.load(input);
  }


  static public void save() {
    try {
      processing.utils.Preferences.save();
    } catch (IOException e) {
      Messages.showWarning("Preferences",
                           "Could not save the Preferences file.", e);
    }
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  // all the information from preferences.txt

  static public String get(String attribute /*, String defaultValue */) {
    if (!isInitialized()) {
        init();
    }
    return processing.utils.Preferences.get(attribute);
  }


  static public String getDefault(String attribute) {
    return processing.utils.Preferences.getDefault(attribute);
  }


  static public void set(String attribute, String value) {
    processing.utils.Preferences.set(attribute, value);
  }


  static public void unset(String attribute) {
    processing.utils.Preferences.unset(attribute);
  }


  static public boolean getBoolean(String attribute) {
    String value = get(attribute); //, null);
    return Boolean.parseBoolean(value);
  }


  static public void setBoolean(String attribute, boolean value) {
    set(attribute, value ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
  }


  static public int getInteger(String attribute /*, int defaultValue*/) {
    try {
      return Integer.parseInt(get(attribute));
    } catch (NumberFormatException err) {
      try {
        return Integer.parseInt(getDefault(attribute));
      } catch (NumberFormatException err2) {
        throw new IllegalArgumentException("Cannot parse: " + attribute);
      }
    }
  }


  static public void setInteger(String key, int value) {
    set(key, String.valueOf(value));
  }


  static public Color getColor(String name) {
    Color parsed = Color.GRAY;  // set a default
    String s = get(name);
    if ((s != null) && (s.indexOf("#") == 0)) { //$NON-NLS-1$
      try {
        parsed = new Color(Integer.parseInt(s.substring(1), 16));
      } catch (Exception ignored) { }
    }
    return parsed;
  }


  static public void setColor(String attr, Color what) {
    set(attr, "#" + PApplet.hex(what.getRGB() & 0xffffff, 6)); //$NON-NLS-1$
  }


  static public Font getFont(String familyAttr, String sizeAttr, int style) {
    int fontSize = getInteger(sizeAttr);

    String fontFamily = get(familyAttr);
    if ("processing.mono".equals(fontFamily) ||
        Toolkit.getMonoFontName().equals(fontFamily)) {
      return Toolkit.getMonoFont(fontSize, style);
    }
    return new Font(fontFamily, style, fontSize);
  }


  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  static public String getOldSketchbookPath() {
    return get("sketchbook.path.three"); //$NON-NLS-1$
  }


  static public String getSketchbookPath() {
    return get("sketchbook.path.four"); //$NON-NLS-1$
  }


  static protected void setSketchbookPath(String path) {
    set("sketchbook.path.four", path); //$NON-NLS-1$
  }
}
