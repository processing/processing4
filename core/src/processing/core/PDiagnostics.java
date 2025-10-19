/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-25 The Processing Foundation

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.core;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * System diagnostics gathering for Processing sketches.
 * <p>
 * This class collects system information, Processing runtime details,
 * and memory statistics for debugging purposes. It's designed to be
 * called from the PDE debugger over the debug connection.
 * </p>
 * 
 * @author Processing Foundation
 * @since 4.4
 */
public class PDiagnostics {
  
  // Privacy-safe system properties to include
  private static final String[] SAFE_PROPERTIES = {
    "java.version",
    "java.vendor",
    "java.vm.name",
    "java.vm.version",
    "java.runtime.name",
    "java.runtime.version",
    "os.name",
    "os.version",
    "os.arch",
    "file.separator",
    "path.separator",
    "line.separator"
  };
  
  
  /**
   * Gathers diagnostic information from a running sketch.
   * 
   * @param applet the PApplet instance to gather diagnostics from
   * @return JSON string containing diagnostic information
   */
  public static String gather(PApplet applet) {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    
    // Timestamp
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    json.append("  \"timestamp\": \"").append(dateFormat.format(new Date())).append("\",\n");
    
    // Processing version (read from system property, set by PDE at launch)
    String processingVersion = System.getProperty("processing.version", "unknown");
    json.append("  \"processingVersion\": ").append(jsonString(processingVersion)).append(",\n");
    
    // System properties
    json.append("  \"system\": {\n");
    boolean first = true;
    for (String prop : SAFE_PROPERTIES) {
      String value = System.getProperty(prop);
      if (value != null) {
        if (!first) json.append(",\n");
        // Sanitize line separator for JSON
        if (prop.equals("line.separator")) {
          value = escapeForJson(value);
        }
        json.append("    \"").append(prop).append("\": ").append(jsonString(value));
        first = false;
      }
    }
    json.append("\n  },\n");
    
    // Memory information
    Runtime runtime = Runtime.getRuntime();
    json.append("  \"memory\": {\n");
    json.append("    \"totalMemory\": ").append(runtime.totalMemory()).append(",\n");
    json.append("    \"freeMemory\": ").append(runtime.freeMemory()).append(",\n");
    json.append("    \"maxMemory\": ").append(runtime.maxMemory()).append(",\n");
    json.append("    \"usedMemory\": ").append(runtime.totalMemory() - runtime.freeMemory()).append("\n");
    json.append("  },\n");
    
    // Sketch-specific information
    if (applet != null) {
      json.append("  \"sketch\": {\n");
      
      // Renderer information
      PGraphics graphics = applet.g;
      if (graphics != null) {
        json.append("    \"renderer\": ").append(jsonString(graphics.getClass().getSimpleName())).append(",\n");
        json.append("    \"width\": ").append(applet.width).append(",\n");
        json.append("    \"height\": ").append(applet.height).append(",\n");
        json.append("    \"pixelDensity\": ").append(applet.pixelDensity).append(",\n");
        json.append("    \"frameCount\": ").append(applet.frameCount).append(",\n");
        json.append("    \"frameRate\": ").append(String.format("%.2f", applet.frameRate)).append(",\n");
      }
      
      json.append("    \"focused\": ").append(applet.focused).append("\n");
      json.append("  },\n");
    }
    
    // Display information
    try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] devices = ge.getScreenDevices();
      
      json.append("  \"displays\": [\n");
      for (int i = 0; i < devices.length; i++) {
        if (i > 0) json.append(",\n");
        GraphicsDevice device = devices[i];
        DisplayMode mode = device.getDisplayMode();
        
        json.append("    {\n");
        json.append("      \"id\": ").append(i).append(",\n");
        json.append("      \"width\": ").append(mode.getWidth()).append(",\n");
        json.append("      \"height\": ").append(mode.getHeight()).append(",\n");
        json.append("      \"refreshRate\": ").append(mode.getRefreshRate()).append(",\n");
        json.append("      \"bitDepth\": ").append(mode.getBitDepth()).append("\n");
        json.append("    }");
      }
      json.append("\n  ]\n");
      
    } catch (Exception e) {
      json.append("  \"displays\": \"Error: ").append(e.getMessage()).append("\"\n");
    }
    
    json.append("}");
    return json.toString();
  }
  
  
  /**
   * Converts a string to JSON-safe quoted string.
   */
  private static String jsonString(String value) {
    if (value == null) {
      return "null";
    }
    return "\"" + escapeForJson(value) + "\"";
  }
  
  
  /**
   * Escapes special characters for JSON.
   */
  private static String escapeForJson(String value) {
    return value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }
  
  
  /**
   * Formats bytes into human-readable format (KB, MB, GB).
   */
  public static String formatBytes(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    char unit = "KMGT".charAt(exp - 1);
    return String.format("%.2f %sB", bytes / Math.pow(1024, exp), unit);
  }
}
