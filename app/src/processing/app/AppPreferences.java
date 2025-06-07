package processing.app;

import processing.utils.Preferences;
import processing.app.ui.Toolkit;

import java.awt.*;

public class AppPreferences extends Preferences {

    static public void init() {
        Preferences.init();

        // For CJK users, enable IM support by default
        if (Language.useInputMethod() && !getBoolean("editor.input_method_support")) {
            setBoolean("editor.input_method_support", true);
        }
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
