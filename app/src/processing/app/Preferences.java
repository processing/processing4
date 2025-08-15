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

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;

import processing.app.ui.Toolkit;
import processing.core.*;
import processing.utils.PreferencesException;


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
public class Preferences extends processing.utils.Preferences {

  static public void init() {
    processing.utils.Preferences.initialized = true;

    try {
      // start by loading the defaults, in case something
      // important was deleted from the user prefs
      loadDefaults();
    } catch (PreferencesException e) {
      Messages.showError(e.getTitle(), e.getMessage(), e);
    }

    // other things that have to be set explicitly for the defaults
    setColor("run.window.bgcolor", SystemColor.control); //$NON-NLS-1$

    // For CJK users, enable IM support by default
    if (Language.useInputMethod()) {
      setBoolean("editor.input_method_support", true);
    }

    // next load user preferences file
    Preferences.setPreferencesFolder(Base.getSettingsFolder());
    try {
      loadPreferences();
    } catch (PreferencesException e) {
      Messages.showError(e.getTitle(), e.getMessage(), e);
    }

    boolean firstRun = !getPreferencesFile().exists();

    if (checkSketchbookPref() || firstRun) {
//    if (firstRun) {
      // create a new preferences file if none exists
      // saves the defaults out to the file
      save();
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


  static void handleProxy(String protocol, String hostProp, String portProp) {
    String proxyHost = get("proxy." + protocol + ".host");
    String proxyPort = get("proxy." + protocol + ".port");
    if (proxyHost != null && proxyHost.length() != 0 &&
        proxyPort != null && proxyPort.length() != 0) {
      System.setProperty(hostProp, proxyHost);
      System.setProperty(portProp, proxyPort);
    }

  }


  static public void save(){
    try {
      processing.utils.Preferences.save();
    } catch (PreferencesException e ) {
      Messages.showWarning(e.getTitle(), e.getMessage(), e);
    }
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


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

}
