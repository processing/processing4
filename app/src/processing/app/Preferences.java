package processing.app;

import processing.app.ui.Toolkit;
import processing.core.PApplet;

import java.awt.*;

public class Preferences extends processing.utils.Preferences {

    static public void init() {
        processing.utils.Preferences.init();

        // For CJK users, enable IM support by default
        if (Language.useInputMethod() && !getBoolean("editor.input_method_support")) {
            setBoolean("editor.input_method_support", true);
        }

        if(get("run.window.bgcolor").isEmpty()){
            setColor("run.window.bgcolor", SystemColor.control);
        }

        if(checkSketchbookPref()){
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


    static public Font getFont(String familyAttr, String sizeAttr, int style) {
        int fontSize = getInteger(sizeAttr);

        String fontFamily = get(familyAttr);
        if ("processing.mono".equals(fontFamily) ||
                Toolkit.getMonoFontName().equals(fontFamily)) {
            return Toolkit.getMonoFont(fontSize, style);
        }
        return new Font(fontFamily, style, fontSize);
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


    public static void setSketchbookPath(String path) {
        set("sketchbook.path.four", path); //$NON-NLS-1$
    }
}
