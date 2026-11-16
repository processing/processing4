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

import java.io.IOException;


/**
 * Minimal stand-in for the app's Preferences so the preprocessor can be
 * used without the PDE. Delegates to the standalone
 * {@link processing.utils.Preferences}, which bundles the default settings,
 * so preferences resolve even when no preferences.txt exists yet.
 */
public class Preferences {

  static private void ensureInitialized() {
    if (!processing.utils.Preferences.isInitialized()) {
      try {
        processing.utils.Preferences.init();
      } catch (IOException e) {
        // The bundled defaults are enough to preprocess with; an unreadable
        // preferences.txt shouldn't stop a build.
        System.err.println("Could not read preferences: " + e.getMessage());
      }
    }
  }


  static public String get(String attribute) {
    ensureInitialized();
    return processing.utils.Preferences.get(attribute);
  }


  static public boolean getBoolean(String attribute) {
    ensureInitialized();
    return processing.utils.Preferences.getBoolean(attribute);
  }


  static public int getInteger(String attribute) {
    ensureInitialized();
    return processing.utils.Preferences.getInteger(attribute);
  }
}
