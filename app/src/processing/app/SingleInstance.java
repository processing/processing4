/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-14 The Processing Foundation
  Copyright (c) 2011-12 Ben Fry and Casey Reas

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

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import processing.core.PApplet;


/**
 * Class that handles a small server that prevents multiple instances of
 * Processing from running simultaneously. If there's already an instance
 * running, it'll handle opening a new empty sketch, or any files that had
 * been passed in on the command line.
 */
public class SingleInstance {
  static final String SERVER_PORT = "instance_server.port";
  static final String SERVER_KEY = "instance_server.key";


  /**
   * Returns true if there's an instance of Processing already running.
   * Will not return true unless this code was able to successfully
   * contact the already running instance to have it launch sketches.
   * @return true if successfully launched on the other instance
   */
  static boolean alreadyRunning(String[] args) {
    return AppPreferences.get(SERVER_PORT) != null && sendArguments(args);
  }


  /**
   * Disable briefly for Processing to restart itself.
   */
  static public void clearRunning() {
    AppPreferences.unset(SERVER_PORT);
    AppPreferences.save();
  }


  static void startServer(final Base base) {
    try {
      AppMessages.log("Opening SingleInstance socket");
      final ServerSocket ss =
        new ServerSocket(0, 0, InetAddress.getLoopbackAddress());
      AppPreferences.set(SERVER_PORT, "" + ss.getLocalPort());
      final String key = "" + Math.random();
      AppPreferences.set(SERVER_KEY, key);
      AppPreferences.save();

      AppMessages.log("Starting SingleInstance thread");
      new Thread(new Runnable() {
        public void run() {
          while (true) {
            try {
              Socket s = ss.accept();  // blocks (sleeps) until connection
              final BufferedReader reader = PApplet.createReader(s.getInputStream());
              String receivedKey = reader.readLine();
              AppMessages.log(this, "key is " + key + ", received is " + receivedKey);

              if (key.equals(receivedKey)) {
                EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    try {
                      AppMessages.log(this, "about to read line");
                      String path = reader.readLine();
                      if (path == null) {
                        // Because an attempt was made to launch the PDE again,
                        // throw the user a bone by at least opening a new
                        // Untitled window for them.
                        AppMessages.log(this, "opening new empty sketch");
//                        platform.base.handleNew();
                        base.handleNew();

                      } else {
                        // loop through the sketches that were passed in
                        do {
                          AppMessages.log(this, "calling open with " + path);
//                        platform.base.handleOpen(filename);
                          base.handleOpen(path);
                          path = reader.readLine();
                        } while (path != null);
                      }
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                });
              } else {
                AppMessages.log(this, "keys do not match");
              }
//              }
            } catch (IOException e) {
              AppMessages.err("SingleInstance error while listening", e);
            }
          }
        }
      }, "SingleInstance Server").start();

    } catch (IOException e) {
      AppMessages.err("Could not create single instance server.", e);
    }
  }


  static boolean sendArguments(String[] args) {  //, long timeout) {
    try {
      AppMessages.log("Checking to see if Processing is already running");
      int port = AppPreferences.getInteger(SERVER_PORT);
      String key = AppPreferences.get(SERVER_KEY);

      Socket socket = null;
      try {
        socket = new Socket(InetAddress.getLoopbackAddress(), port);
      } catch (Exception ignored) { }

      if (socket != null) {
        AppMessages.log("Processing is already running, sending command line");
        PrintWriter writer = PApplet.createWriter(socket.getOutputStream());
        writer.println(key);
        for (String arg : args) {
          writer.println(arg);
        }
        writer.flush();
        writer.close();
        return true;
      }
    } catch (IOException e) {
      AppMessages.err("Error sending commands to other instance", e);
    }
    AppMessages.log("Processing is not already running (or could not connect)");
    return false;
  }
}
