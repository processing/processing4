/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org
  Copyright (c) 2012-25 The Processing Foundation

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation, Inc.
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/

package processing.mode.java.debug;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import processing.app.Language;
import processing.mode.java.JavaEditor;


/**
 * Dialog for displaying system diagnostics gathered from a running sketch.
 * <p>
 * Provides a formatted view of diagnostic information including system
 * properties, memory statistics, sketch details, and display information.
 * Users can copy the diagnostics to clipboard or export to a text file.
 * </p>
 * 
 * @author Processing Foundation
 * @since 4.4
 */
public class DiagnosticsDialog extends JDialog {
  private JTextArea textArea;
  private String diagnosticData;
  private JavaEditor editor;
  
  
  public DiagnosticsDialog(JavaEditor editor, String diagnosticData) {
    super(editor, Language.text("menu.debug.gather_diagnostics"), false);
    this.editor = editor;
    this.diagnosticData = diagnosticData;
    
    setLayout(new BorderLayout());
    
    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    textArea.setText(formatDiagnostics(diagnosticData));
    textArea.setCaretPosition(0);
    
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(600, 500));
    add(scrollPane, BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    
    JButton copyButton = new JButton(Language.text("menu.edit.copy"));
    copyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        copyToClipboard();
      }
    });
    buttonPanel.add(copyButton);
    
    JButton exportButton = new JButton(Language.text("prompt.export"));
    exportButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        exportToFile();
      }
    });
    buttonPanel.add(exportButton);
    
    JButton closeButton = new JButton(Language.text("menu.file.close"));
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonPanel.add(closeButton);
    
    add(buttonPanel, BorderLayout.SOUTH);
    
    pack();
    setLocationRelativeTo(editor);
  }
  
  
  /**
   * Formats the JSON diagnostic data into a human-readable format.
   */
  private String formatDiagnostics(String jsonData) {
    if (jsonData == null || jsonData.isEmpty()) {
      return "No diagnostic data available.";
    }
    
    StringBuilder formatted = new StringBuilder();
    formatted.append("System Diagnostics\n");
    formatted.append("==================\n\n");
    
    String[] lines = jsonData.split("\n");
    int indentLevel = 0;
    
    for (String line : lines) {
      String trimmed = line.trim();
      
      if (trimmed.startsWith("}") || trimmed.startsWith("]")) {
        indentLevel--;
      }
      
      if (trimmed.length() > 0 && !trimmed.equals("{") && !trimmed.equals("}") 
          && !trimmed.equals("[") && !trimmed.equals("]") 
          && !trimmed.equals("},") && !trimmed.equals("],")) {
        
        String display = trimmed.replaceAll("\"", "")
                               .replaceAll(",", "")
                               .replaceAll(":", ": ");
        
        for (int i = 0; i < indentLevel; i++) {
          formatted.append("  ");
        }
        
        formatted.append(display).append("\n");
      }
      
      if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
        if (trimmed.contains(":")) {
          String sectionName = trimmed.substring(0, trimmed.indexOf(":"));
          sectionName = sectionName.replaceAll("\"", "");
          formatted.append("\n").append(sectionName.toUpperCase()).append(":\n");
        }
        indentLevel++;
      }
    }
    
    formatted.append("\n");
    formatted.append("==================\n");
    formatted.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
    
    return formatted.toString();
  }
  
  
  /**
   * Copies diagnostic data to system clipboard.
   */
  private void copyToClipboard() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection selection = new StringSelection(textArea.getText());
      clipboard.setContents(selection, null);
      
      editor.statusNotice("Diagnostics copied to clipboard.");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
                                   "Could not copy diagnostics to clipboard.\n" + e.getMessage(),
                                   "Copy Failed",
                                   JOptionPane.WARNING_MESSAGE);
    }
  }
  
  
  /**
   * Exports diagnostic data to a text file.
   */
  private void exportToFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Diagnostics");
    
    // Default filename with timestamp
    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    fileChooser.setSelectedFile(new File("diagnostics_" + timestamp + ".txt"));
    
    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(textArea.getText());
        writer.write("\n\n");
        writer.write("Raw JSON Data:\n");
        writer.write("==============\n");
        writer.write(diagnosticData);
        
        editor.statusNotice("Diagnostics exported to " + file.getName());
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
                                     "Could not export diagnostics to file.\n" + e.getMessage(),
                                     "Export Failed",
                                     JOptionPane.WARNING_MESSAGE);
      }
    }
  }
}
