/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */ /*
  Part of the Processing project - http://processing.org

  Copyright (c) 2015 The Processing Foundation

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  version 2, as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package processing.utils

import java.awt.Frame
import javax.swing.JOptionPane

open class Messages {
    companion object {
        /**
         * "No cookie for you" type messages. Nothing fatal or all that
         * much of a bummer, but something to notify the user about.
         */
        @JvmStatic
        fun showMessage(title: String = "Message", message: String) {
            if (Base.isCommandLine()) {
                println("$title: $message")
            } else {
                JOptionPane.showMessageDialog(
                    Frame(), message, title,
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        }


        /**
         * Non-fatal error message with optional stack trace side dish.
         */
        /**
         * Non-fatal error message.
         */
        @JvmStatic
        @JvmOverloads
        fun showWarning(title: String = "Warning", message: String, e: Throwable? = null) {
            if (Base.isCommandLine()) {
                println("$title: $message")
            } else {
                JOptionPane.showMessageDialog(
                    Frame(), message, title,
                    JOptionPane.WARNING_MESSAGE
                )
            }
            e?.printStackTrace()
        }


        /**
         * Show an error message that's actually fatal to the program.
         * This is an error that can't be recovered. Use showWarning()
         * for errors that allow P5 to continue running.
         */
        @JvmStatic
        fun showError(title: String? = "Error", message: String, e: Throwable?) {
            if (Base.isCommandLine()) {
                System.err.println("$title: $message")
            } else {
                JOptionPane.showMessageDialog(
                    Frame(), message, title,
                    JOptionPane.ERROR_MESSAGE
                )
            }
            e?.printStackTrace()
            System.exit(1)
        }


        // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
        @JvmStatic
        @Deprecated("Use log() instead")
        fun log(from: Any, message: String) {
            if (Base.DEBUG) {
                val callingClass = Throwable()
                    .stackTrace[2]
                    .className
                    .formatClassName()
                println("$callingClass: $message")
            }
        }

        @JvmStatic
        fun log(message: String?) {
            if (Base.DEBUG) {
                val callingClass = Throwable()
                    .stackTrace[2]
                    .className
                    .formatClassName()
                println("$callingClass$message")
            }
        }

        @JvmStatic
        fun logf(message: String?, vararg args: Any?) {
            if (Base.DEBUG) {
                val callingClass = Throwable()
                    .stackTrace[2]
                    .className
                    .formatClassName()
                System.out.printf("$callingClass$message", *args)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun err(message: String?, e: Throwable? = null) {
            if (Base.DEBUG) {
                if (message != null) {
                    val callingClass = Throwable()
                        .stackTrace[4]
                        .className
                        .formatClassName()
                    System.err.println("$callingClass$message")
                }
                e?.printStackTrace()
            }
        }
    }
}

// Helper functions to give the base classes a color
fun String.formatClassName() = this
    .replace("processing.", "")
    .replace(".", "/")
    .padEnd(40)
    .colorizePathParts()
fun String.colorizePathParts() = split("/").joinToString("/") { part ->
    "\u001B[${31 + (part.hashCode() and 0x7).rem(6)}m$part\u001B[0m"
}