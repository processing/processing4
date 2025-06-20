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
package processing.app

import processing.app.ui.Toolkit
import processing.utils.Messages

import java.awt.EventQueue
import java.awt.Frame
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JFrame
import javax.swing.JOptionPane

class AppMessages : Messages() {
    companion object {
        /**
         * Non-fatal error message with two levels of formatting.
         * Unlike the others, this is non-blocking and will run later on the EDT.
         */
        @JvmStatic
        fun showWarningTiered(
            title: String,
            primary: String, secondary: String,
            e: Throwable?
        ) {
            if (Base.isCommandLine()) {
                // TODO All these messages need to be handled differently for
                //      proper parsing on the command line. Many have \n in them.
                println("$title: $primary\n$secondary")
            } else {
                EventQueue.invokeLater {
                    JOptionPane.showMessageDialog(
                        JFrame(),
                        Toolkit.formatMessage(primary, secondary),
                        title, JOptionPane.WARNING_MESSAGE
                    )
                }
            }
            e?.printStackTrace()
        }


        /**
         * Warning window that includes the stack trace.
         */
        @JvmStatic
        fun showTrace(
            title: String?,
            message: String,
            t: Throwable?,
            fatal: Boolean
        ) {
            val title = title ?: if (fatal) "Error" else "Warning"

            if (Base.isCommandLine()) {
                System.err.println("$title: $message")
                t?.printStackTrace()
            } else {
                val sw = StringWriter()
                t!!.printStackTrace(PrintWriter(sw))

                JOptionPane.showMessageDialog(
                    Frame(),  // first <br/> clears to the next line
                    // second <br/> is a shorter height blank space before the trace
                    Toolkit.formatMessage("$message<br/><tt><br/>$sw</tt>"),
                    title,
                    if (fatal) JOptionPane.ERROR_MESSAGE else JOptionPane.WARNING_MESSAGE
                )

                if (fatal) {
                    System.exit(1)
                }
            }
        }

        @JvmStatic
        fun showYesNoQuestion(
            editor: Frame?, title: String?,
            primary: String?, secondary: String?
        ): Int {
            if (!Platform.isMacOS()) {
                return JOptionPane.showConfirmDialog(
                    editor,
                    Toolkit.formatMessage(primary, secondary),  //"<html><body>" +
                    //"<b>" + primary + "</b>" +
                    //"<br>" + secondary,
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                )
            } else {
                val result = showCustomQuestion(
                    editor, title, primary, secondary,
                    0, "Yes", "No"
                )
                return if (result == 0) {
                    JOptionPane.YES_OPTION
                } else if (result == 1) {
                    JOptionPane.NO_OPTION
                } else {
                    JOptionPane.CLOSED_OPTION
                }
            }
        }


        /**
         * @param highlight A valid array index for options[] that specifies the
         * default (i.e. safe) choice.
         * @return The (zero-based) index of the selected value, -1 otherwise.
         */
        @JvmStatic
        fun showCustomQuestion(
            editor: Frame?, title: String?,
            primary: String?, secondary: String?,
            highlight: Int, vararg options: String
        ): Int {
            val result: Any
            if (!Platform.isMacOS()) {
                return JOptionPane.showOptionDialog(
                    editor,
                    Toolkit.formatMessage(primary, secondary), title,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, options[highlight]
                )
            } else {
                val pane =
                    JOptionPane(
                        Toolkit.formatMessage(primary, secondary),
                        JOptionPane.QUESTION_MESSAGE
                    )

                pane.options = options

                // highlight the safest option ala apple hig
                pane.initialValue = options[highlight]

                val dialog = pane.createDialog(editor, null)
                dialog.isVisible = true

                result = pane.value
            }
            for (i in options.indices) {
                if (result != null && result == options[i]) return i
            }
            return -1
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