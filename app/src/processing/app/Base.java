/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-23 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  version 2, as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 /* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-23 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

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

package processing.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.jetbrains.annotations.NotNull;
import processing.app.contrib.*;
import processing.app.tools.Tool;
import processing.app.ui.*;
import processing.app.ui.Toolkit;
import processing.core.PApplet;
import processing.data.StringList;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

/**
 * The base class for the main processing application.
 * Primary role of this class is for platform identification and
 * general interaction with the system (launching URLs, loading
 * files and images, etc.) that comes from that.
 */
public class Base {
  /**
   * Revision number, used for update checks and contribution compatibility.
   */
  static private final int REVISION = Integer.parseInt(System.getProperty("processing.revision", "1295"));
  /**
   * This might be replaced by main() if there's a lib/version.txt file.
   */
  static private String VERSION_NAME = System.getProperty("processing.version", "1295");

  static final public String SKETCH_BUNDLE_EXT = ".pdez";
  static final public String CONTRIB_BUNDLE_EXT = ".pdex";

  /**
   * True if heavy debugging error/log messages are enabled. Set to true
   * if an empty file named 'debug' is found in the settings folder.
   * See implementation in createAndShowGUI().
   */
  static public boolean DEBUG = Boolean.parseBoolean(System.getenv().getOrDefault("DEBUG", "false"));

  /**
   * is Processing being run from the command line (true) or from the GUI (false)?
   */
  static private boolean commandLine;

  /**
   * If settings.txt is present inside lib, it will be used to override
   * the location of the settings folder so that "portable" versions
   * of the software are possible.
   */
  static private File settingsOverride;

  // A single instance of the preferences window
  PreferencesFrame preferencesFrame;

  // Location for untitled items
  static File untitledFolder;

  /** List of currently active editors. */
  final protected List<Editor> editors =
          Collections.synchronizedList(new ArrayList<>());
  protected Editor activeEditor;

  /** A lone file menu to be used when all sketch windows are closed. */
  protected JMenu defaultFileMenu;

  /**
   * The next Mode to be used with handleNew() or handleOpen()
   * (unless it's overridden by something else). Starts with the last
   * Mode used with the environment, or the default mode if not used.
   */
  private Mode nextMode;

  /** Only one built-in Mode these days, removing the extra fluff. */
  private Mode coreMode;

  private List<ModeContribution> contribModes;
  private List<ExamplesContribution> contribExamples;

  /** These aren't even dynamically loaded, they're hard-wired here. */
  private List<Tool> internalTools;

  private List<ToolContribution> coreTools;
  private List<ToolContribution> contribTools;

  /** Current tally of available updates (used for new Editor windows). */
  private int updatesAvailable = 0;

  // Used by handleOpen(), this saves the chooser to remember the directory.
  private JFileChooser openChooser;

  static public void main(final String[] args) {
    Messages.log("Starting Processing version" + VERSION_NAME + " revision "+ REVISION);
    EventQueue.invokeLater(() -> {
      run(args);
    });
  }

  /**
   * The main run() method, wrapped in a try/catch to
   * provide a graceful error message if something goes wrong.
   */
  private static void run(String[] args) {
    try {
      createAndShowGUI(args);
    } catch (Throwable t) {
      if (Platform.isWindows()) {
        String mess = t.getMessage();
        String missing = null;
        if (mess.contains("Could not initialize class com.sun.jna.Native")) {
          missing = "jnidispatch.dll";
        } else if (t instanceof NoClassDefFoundError &&
                mess.contains("processing/core/PApplet")) {
          missing = "core.jar";
        }
        if (missing != null) {
          Messages.showError("Necessary files are missing",
                  "A file required by Processing (" + missing + ") is missing.\n\n" +
                          "Make sure that you're not trying to run Processing from inside\n" +
                          "the .zip file you downloaded, and check that Windows Defender\n" +
                          "has not removed files from the Processing folder.\n\n" +
                          "(Defender sometimes flags parts of Processing as malware.\n" +
                          "It is not, but Microsoft has ignored our pleas for help.)", t);
        }
      }
      Messages.showTrace("Unknown Problem",
              "A serious error happened during startup. Please report:\n" +
                      "http://github.com/processing/processing4/issues/new", t, true);
    }
  }


  static private void createAndShowGUI(String[] args) {
    checkVersion();
    checkPortable();
    Platform.init();
    Console.startup();
    Language.init();
    Preferences.init();
    PreferencesEvents.onUpdated(Preferences::init);

    boolean createNewInstance = DEBUG || !SingleInstance.alreadyRunning(args);
    if (!createNewInstance) {
      System.exit(0);
      return;
    }

    setLookAndFeel();
    locateSketchbookFolder();
    Theme.init();
    setupUntitleSketches();

    Messages.log("About to create Base...");
    try {
      final Base base = new Base(args);
      base.updateTheme();
      Messages.log("Base() constructor succeeded");
      SingleInstance.startServer(base);
      handleWelcomeScreen(base);
      handleCrustyDisplay();
      handleTempCleaning();
    } catch (Throwable t) {
      Throwable err = t;
      if (t.getCause() != null) {
        err = t.getCause();
      }
      Messages.showTrace("We're off on the wrong foot",
              "An error occurred during startup.", err, true);
    }
    Messages.log("Done creating Base...");
  }

  private static void setupUntitleSketches() {
    try {
      String uuid = UUID.randomUUID().toString();
      untitledFolder = new File(Util.getProcessingTemp(), uuid);
    } catch (IOException e) {
      Messages.showError("Trouble without a name",
              "Could not create a place to store untitled sketches.\n" +
                      "That's gonna prevent us from continuing.", e);
    }
  }

  private static void setLookAndFeel() {
    try {
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      Platform.setLookAndFeel();
      Platform.setInterfaceZoom();
    } catch (Exception e) {
      Messages.err("Error while setting up the interface", e);
    }
  }


  public void updateTheme() {
    try {
      FlatLaf laf = "dark".equals(Theme.get("laf.mode")) ?
              new FlatDarkLaf() : new FlatLightLaf();
      laf.setExtraDefaults(Collections.singletonMap("@accentColor",
              Theme.get("laf.accent.color")));
      FlatLaf.setup(laf);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (preferencesFrame != null) {
      preferencesFrame.updateTheme();
    }

    ContributionManager.updateTheme();
    for (Editor editor : getEditors()) {
      editor.updateTheme();
    }
  }


  static private void handleWelcomeScreen(Base base) {
    if (Preferences.getBoolean("welcome.four.show")) {
      PDEWelcomeKt.showWelcomeScreen(base);
    }
  }


  static private void handleCrustyDisplay() {
    if (Platform.isWindows()) {
      if (!Toolkit.isRetina() && !Splash.getDisableHiDPI()) {
        int res = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
        if (res % 96 != 0) {
          System.out.println("If the editor cursor is in the wrong place or the interface is blocky or fuzzy,");
          System.out.println("open Preferences and select the \u201cDisable HiDPI Scaling\u201d option to fix it.");
        }
      }
    }
  }


  static private void handleTempCleaning() {
    new Thread(() -> {
      Console.cleanTempFiles();
      cleanTempFolders();
    }).start();
  }


  static public void cleanTempFolders() {
    try {
      final File tempDir = Util.getProcessingTemp();
      final int days = Preferences.getInteger("temp.days");

      if (days > 0) {
        final long now = new Date().getTime();
        final long diff = days * 24 * 60 * 60 * 1000L;
        File[] expiredFiles =
                tempDir.listFiles(file -> (now - file.lastModified()) > diff);
        if (expiredFiles != null) {
          for (File file : expiredFiles) {
            try {
              Platform.deleteFile(file);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void checkVersion() {
    File versionFile = Platform.getContentFile("lib/version.txt");
    if (versionFile != null && versionFile.exists()) {
      String[] lines = PApplet.loadStrings(versionFile);
      if (lines != null && lines.length > 0) {
        if (!VERSION_NAME.equals(lines[0])) {
          VERSION_NAME = lines[0];
        }
      }
    }
  }

  static void checkPortable() {
    File settingsFile = Platform.getContentFile("lib/settings.txt");
    if (settingsFile != null && settingsFile.exists()) {
      try {
        Settings portable = new Settings(settingsFile);
        String path = portable.get("settings.path");
        File folder = new File(path);
        boolean success = true;
        if (!folder.exists()) {
          success = folder.mkdirs();
          if (!success) {
            Messages.err("Could not create " + folder + " to store settings.");
          }
        }
        if (success) {
          if (!folder.canRead()) {
            Messages.err("Cannot read from " + folder);
          } else if (!folder.canWrite()) {
            Messages.err("Cannot write to " + folder);
          } else {
            settingsOverride = folder.getAbsoluteFile();
          }
        }
      } catch (IOException e) {
        Messages.err("Error while reading the settings.txt file", e);
      }
    }
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  static public int getRevision() {
    return REVISION;
  }

  static public String getVersionName() {
    return VERSION_NAME;
  }

  public static void setCommandLine() {
    commandLine = true;
  }

  static public boolean isCommandLine() {
    return commandLine;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public Base(String[] args) throws Exception {
    ContributionManager.init(this);
    buildCoreModes();
    rebuildContribModes();
    rebuildContribExamples();
    rebuildToolList();
    Recent.init(this);
    setupNextMode();
    nextMode.rebuildLibraryList();
    Platform.initBase(this);
    UpdateCheck.doCheck(this);
    ContributionListing cl = ContributionListing.getInstance();
    cl.downloadAvailableList(this, new ContribProgress(null));
    openFilesOrNew(args);
  }

  private void openFilesOrNew(String[] args) {
    boolean opened = false;

    for (int i = 0; i < args.length; i++) {
      Messages.logf("Parsing command line... args[%d] = '%s'", i, args[i]);

      String path = args[i];
      if (Platform.isWindows()) {
        try {
          File file = new File(args[i]);
          path = file.getCanonicalPath();
          Messages.logf("Changing %s to canonical %s", i, args[i], path);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (handleOpen(path) != null) {
        opened = true;
      }
    }

    if (!opened) {
      Messages.log("Calling handleNew() to open a new window");
      handleNew();
    } else {
      Messages.log("No handleNew(), something passed on the command line");
    }
  }

  private void setupNextMode() {
    String lastModeIdentifier = Preferences.get("mode.last");
    if (lastModeIdentifier == null) {
      nextMode = getDefaultMode();
      Messages.log("Nothing set for last.sketch.mode, using default.");
    } else {
      for (Mode m : getModeList()) {
        if (m.getIdentifier().equals(lastModeIdentifier)) {
          Messages.logf("Setting next mode to %s.", lastModeIdentifier);
          nextMode = m;
        }
      }
      if (nextMode == null) {
        nextMode = getDefaultMode();
        Messages.logf("Could not find mode %s, using default.", lastModeIdentifier);
      }
    }
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public JMenu initDefaultFileMenu() {
    defaultFileMenu = new JMenu(Language.text("menu.file"));

    JMenuItem item = Toolkit.newJMenuItem(Language.text("menu.file.new"), 'N');
    item.addActionListener(e -> handleNew());
    defaultFileMenu.add(item);

    item = Toolkit.newJMenuItem(Language.text("menu.file.open"), 'O');
    item.addActionListener(e -> handleOpenPrompt());
    defaultFileMenu.add(item);

    item = Toolkit.newJMenuItemShift(Language.text("menu.file.sketchbook"), 'K');
    item.addActionListener(e -> showSketchbookFrame());
    defaultFileMenu.add(item);

    item = Toolkit.newJMenuItemShift(Language.text("menu.file.examples"), 'O');
    item.addActionListener(e -> showExamplesFrame());
    defaultFileMenu.add(item);

    return defaultFileMenu;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public void checkFirstEditor(Editor editor) {
    if (activeEditor == null) {
      activeEditor = editor;
    }
  }

  public Editor getActiveEditor() {
    return activeEditor;
  }

  public List<Editor> getEditors() {
    return editors;
  }

  public void handleActivated(Editor whichEditor) {
    activeEditor = whichEditor;
    EditorConsole.setEditor(activeEditor);
    nextMode = whichEditor.getMode();
    Preferences.set("mode.last", nextMode.getIdentifier());
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public void refreshContribs(ContributionType ct) {
    if (ct == ContributionType.LIBRARY) {
      for (Mode m : getModeList()) {
        m.rebuildImportMenu();
      }
    } else if (ct == ContributionType.MODE) {
      rebuildContribModes();
      for (Editor editor : editors) {
        editor.rebuildModePopup();
      }
    } else if (ct == ContributionType.TOOL) {
      rebuildToolList();
      for (Editor editor : editors) {
        populateToolsMenu(editor.getToolMenu());
      }
    } else if (ct == ContributionType.EXAMPLES) {
      rebuildContribExamples();
      for (Mode m : getModeList()) {
        m.rebuildExamplesFrame();
      }
    }
  }


  public Set<Contribution> getInstalledContribs() {
    List<ModeContribution> modeContribs = getContribModes();
    Set<Contribution> contributions = new HashSet<>(modeContribs);

    for (ModeContribution modeContrib : modeContribs) {
      Mode mode = modeContrib.getMode();
      contributions.addAll(mode.contribLibraries);
      contributions.addAll(mode.foundationLibraries);
    }

    contributions.addAll(getContribTools());
    contributions.addAll(getContribExamples());
    return contributions;
  }


  public void tallyUpdatesAvailable() {
    Set<Contribution> installed = getInstalledContribs();
    ContributionListing listing = ContributionListing.getInstance();

    int newCount = 0;
    for (Contribution contrib : installed) {
      if (listing.hasUpdates(contrib)) {
        newCount++;
      }
    }
    updatesAvailable = newCount;

    synchronized (editors) {
      for (Editor editor : editors) {
        editor.setUpdatesAvailable(updatesAvailable);
      }
    }
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public List<Mode> getModeList() {
    List<Mode> outgoing = new ArrayList<>();
    outgoing.add(coreMode);
    if (contribModes != null) {
      for (ModeContribution contrib : contribModes) {
        outgoing.add(contrib.getMode());
      }
    }
    return outgoing;
  }


  void buildCoreModes() {
    ModeContribution javaModeContrib =
            ModeContribution.load(this, Platform.getContentFile("modes/java"),
                    getDefaultModeIdentifier());
    if (javaModeContrib == null) {
      Messages.showError("Startup Error",
              "Could not load Java Mode, please reinstall Processing.",
              new Exception("ModeContribution.load() was null"));
    } else {
      coreMode = javaModeContrib.getMode();
    }
  }


  public List<ModeContribution> getContribModes() {
    return contribModes;
  }


  void rebuildContribModes() {
    if (contribModes == null) {
      contribModes = new ArrayList<>();
    }
    File modesFolder = getSketchbookModesFolder();
    Map<File, ModeContribution> known = new HashMap<>();
    for (ModeContribution contrib : getContribModes()) {
      known.put(contrib.getFolder(), contrib);
    }
    File[] potential = ContributionType.MODE.listCandidates(modesFolder);
    if (potential != null) {
      for (File folder : potential) {
        if (!known.containsKey(folder)) {
          try {
            contribModes.add(new ModeContribution(this, folder, null));
          } catch (NoSuchMethodError | NoClassDefFoundError ne) {
            System.err.println(folder.getName() + " is not compatible with this version of Processing");
            if (DEBUG) ne.printStackTrace();
          } catch (InvocationTargetException ite) {
            System.err.println(folder.getName() + " could not be loaded and may not compatible with this version of Processing");
            if (DEBUG) ite.printStackTrace();
          } catch (IgnorableException ig) {
            Messages.log(ig.getMessage());
            if (DEBUG) ig.printStackTrace();
          } catch (Throwable e) {
            System.err.println("Could not load Mode from " + folder);
            e.printStackTrace();
          }
        } else {
          known.remove(folder);
        }
      }
    }

    final String useMode = System.getProperty("usemode");
    if (useMode != null) {
      final String[] modeInfo = useMode.split(":", 2);
      final String modeClass = modeInfo[0];
      final String modeResourcePath = modeInfo[1];
      System.out.println("Attempting to load " + modeClass + " with resources at " + modeResourcePath);
      ModeContribution mc = ModeContribution.load(this, new File(modeResourcePath), modeClass);
      contribModes.add(mc);
      File key = getModeContribFile(mc, known);
      if (key != null) {
        known.remove(key);
      }
    }
    if (known.size() != 0) {
      for (ModeContribution mc : known.values()) {
        System.out.println("Extraneous Mode entry: " + mc.getName());
      }
    }
  }


  static private File getModeContribFile(ModeContribution contrib,
                                         Map<File, ModeContribution> known) {
    for (Entry<File, ModeContribution> entry : known.entrySet()) {
      if (entry.getValue() == contrib) {
        return entry.getKey();
      }
    }
    return null;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public List<ToolContribution> getCoreTools() {
    return coreTools;
  }

  public List<ToolContribution> getContribTools() {
    return contribTools;
  }


  public void rebuildToolList() {
    if (internalTools == null) {
      internalTools = new ArrayList<>();
      initInternalTool(processing.app.tools.Archiver.class);
      initInternalTool(processing.app.tools.ColorSelector.class);
      initInternalTool(processing.app.tools.CreateFont.class);
      if (Platform.isMacOS()) {
        initInternalTool(processing.app.tools.InstallCommander.class);
      }
      initInternalTool(processing.app.tools.ThemeSelector.class);
    }

    if (coreTools == null) {
      coreTools = ToolContribution.loadAll(Base.getToolsFolder());
      for (Tool tool : coreTools) {
        tool.init(this);
      }
    }

    contribTools = ToolContribution.loadAll(Base.getSketchbookToolsFolder());
    for (Tool tool : contribTools) {
      try {
        tool.init(this);
      } catch (VerifyError | AbstractMethodError ve) {
        System.err.println("\"" + tool.getMenuTitle() + "\" is not " +
                "compatible with this version of Processing");
        Messages.err("Incompatible Tool found during tool.init()", ve);
      } catch (NoSuchMethodError nsme) {
        System.err.println("\"" + tool.getMenuTitle() + "\" is not " +
                "compatible with this version of Processing");
        System.err.println("The " + nsme.getMessage() + " method no longer exists.");
        Messages.err("Incompatible Tool found during tool.init()", nsme);
      } catch (NoClassDefFoundError ncdfe) {
        System.err.println("\"" + tool.getMenuTitle() + "\" is not " +
                "compatible with this version of Processing");
        System.err.println("The " + ncdfe.getMessage() + " class is no longer available.");
        Messages.err("Incompatible Tool found during tool.init()", ncdfe);
      } catch (Error | Exception e) {
        System.err.println("An error occurred inside \"" + tool.getMenuTitle() + "\"");
        e.printStackTrace();
      }
    }
  }


  protected void initInternalTool(Class<?> toolClass) {
    try {
      final Tool tool = (Tool)
              toolClass.getDeclaredConstructor().newInstance();
      tool.init(this);
      internalTools.add(tool);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void clearToolMenus() {
    for (Editor ed : editors) {
      ed.clearToolMenu();
    }
  }


  public void populateToolsMenu(JMenu toolsMenu) {
    if (internalTools == null) {
      rebuildToolList();
    }

    toolsMenu.removeAll();
    for (Tool tool : internalTools) {
      toolsMenu.add(createToolItem(tool));
    }
    toolsMenu.addSeparator();

    if (!coreTools.isEmpty()) {
      for (Tool tool : coreTools) {
        toolsMenu.add(createToolItem(tool));
      }
      toolsMenu.addSeparator();
    }

    if (!contribTools.isEmpty()) {
      for (Tool tool : contribTools) {
        toolsMenu.add(createToolItem(tool));
      }
      toolsMenu.addSeparator();
    }

    JMenuItem manageTools =
            new JMenuItem(Language.text("menu.tools.manage_tools"));
    manageTools.addActionListener(e -> ContributionManager.openTools());
    toolsMenu.add(manageTools);
  }


  JMenuItem createToolItem(final Tool tool) {
    String title = tool.getMenuTitle();
    final JMenuItem item = new JMenuItem(title);
    item.addActionListener(e -> {
      try {
        tool.run();
      } catch (NoSuchMethodError | NoClassDefFoundError ne) {
        Messages.showWarning("Tool out of date",
                tool.getMenuTitle() + " is not compatible with this version of Processing.\n" +
                        "Try updating the Mode or contact its author for a new version.", ne);
        Messages.err("Incompatible tool found during tool.run()", ne);
        item.setEnabled(false);
      } catch (Exception ex) {
        activeEditor.statusError("An error occurred inside \"" + tool.getMenuTitle() + "\"");
        ex.printStackTrace();
        item.setEnabled(false);
      }
    });
    return item;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  void rebuildContribExamples() {
    contribExamples =
            ExamplesContribution.loadAll(getSketchbookExamplesFolder());
  }

  public List<ExamplesContribution> getContribExamples() {
    return contribExamples;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  String getDefaultModeIdentifier() {
    return "processing.mode.java.JavaMode";
  }

  public Mode getDefaultMode() {
    return coreMode;
  }


  public boolean changeMode(Mode mode) {
    Mode oldMode = activeEditor.getMode();
    if (oldMode != mode) {
      Sketch sketch = activeEditor.getSketch();
      nextMode = mode;

      if (sketch.isModified()) {
        handleNew();
        return false;
      } else if (sketch.isUntitled()) {
        handleClose(activeEditor, true);
        handleNew();
      } else {
        if (mode.canEdit(sketch)) {
          sketch.updateModeProperties(nextMode, getDefaultMode());
          handleClose(activeEditor, true);
          Editor editor = handleOpen(sketch.getMainPath());
          if (editor == null) {
            sketch.updateModeProperties(oldMode, getDefaultMode());
            handleOpen(sketch.getMainPath());
            return false;
          }
        } else {
          handleNew();
          return false;
        }
      }
    }
    return true;
  }


  protected Mode findMode(String id) {
    for (Mode mode : getModeList()) {
      if (mode.getIdentifier().equals(id)) {
        return mode;
      }
    }
    return null;
  }

  public void modeRemoved(Mode mode) {
    if (nextMode == mode) {
      nextMode = getDefaultMode();
    }
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  /**
   * Create a new untitled document in a new sketch window.
   */
  public void handleNew() {
    try {
      File newbieDir = SketchName.nextFolder(untitledFolder);
      if (newbieDir == null) return;

      if (!newbieDir.mkdirs()) {
        throw new IOException("Could not create directory " + newbieDir);
      }

      String newbieName = newbieDir.getName();
      File newbieFile = nextMode.addTemplateFiles(newbieDir, newbieName);

      if (!nextMode.equals(getDefaultMode())) {
        Sketch.updateModeProperties(newbieDir, nextMode, getDefaultMode());
      }

      String path = newbieFile.getAbsolutePath();
      handleOpenUntitled(path);

    } catch (IOException e) {
      Messages.showTrace("That's new to me",
              "A strange and unexplainable error occurred\n" +
                      "while trying to create a new sketch.", e, false);
    }
  }


  /**
   * Prompt for a sketch to open, and open it in a new window.
   */
  public void handleOpenPrompt() {
    final StringList extensions = new StringList();
    extensions.append(SKETCH_BUNDLE_EXT);

    for (Mode mode : getModeList()) {
      extensions.append(mode.getDefaultExtension());
    }

    final String prompt = Language.text("open");

    if (Preferences.getBoolean("chooser.files.native")) {
      FileDialog openDialog =
              new FileDialog(activeEditor, prompt, FileDialog.LOAD);

      openDialog.setFilenameFilter((dir, name) -> {
        for (String ext : extensions) {
          if (name.toLowerCase().endsWith("." + ext)) {
            return true;
          }
        }
        return false;
      });

      openDialog.setVisible(true);

      String directory = openDialog.getDirectory();
      String filename = openDialog.getFile();
      if (filename != null) {
        File inputFile = new File(directory, filename);
        handleOpen(inputFile.getAbsolutePath());
      }

    } else {
      if (openChooser == null) {
        openChooser = new JFileChooser();
        openChooser.setDialogTitle(prompt);
      }

      openChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
        public boolean accept(File file) {
          if (file.isDirectory()) {
            return true;
          }
          for (String ext : extensions) {
            if (file.getName().toLowerCase().endsWith("." + ext)) {
              return true;
            }
          }
          return false;
        }

        public String getDescription() {
          return "Processing Sketch";
        }
      });
      if (openChooser.showOpenDialog(activeEditor) == JFileChooser.APPROVE_OPTION) {
        handleOpen(openChooser.getSelectedFile().getAbsolutePath());
      }
    }
  }


  private Editor openSketchBundle(String path) {
    File zipFile = new File(path);
    try {
      untitledFolder.mkdirs();
      File destFolder = File.createTempFile("zip", "tmp", untitledFolder);
      if (!destFolder.delete() || !destFolder.mkdirs()) {
        System.err.println("Could not create temporary folder " + destFolder);
        return null;
      }
      Util.unzip(zipFile, destFolder);
      File[] fileList = destFolder.listFiles(File::isDirectory);
      if (fileList != null) {
        if (fileList.length == 1) {
          File sketchFile = Sketch.findMain(fileList[0], getModeList());
          if (sketchFile != null) {
            return handleOpenUntitled(sketchFile.getAbsolutePath());
          }
        } else {
          System.err.println("Expecting one folder inside " +
                  SKETCH_BUNDLE_EXT + " file, found " + fileList.length + ".");
        }
      } else {
        System.err.println("Could not read " + destFolder);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }


  private void openContribBundle(String path) {
    EventQueue.invokeLater(() -> {
      Editor editor = getActiveEditor();
      if (editor == null) {
        Messages.showWarning("Failure is the only option",
                "Please open an Editor window before installing an extension.");
      } else {
        File contribFile = new File(path);
        String baseName = contribFile.getName();
        baseName = baseName.substring(0, baseName.length() - CONTRIB_BUNDLE_EXT.length());
        int result =
                Messages.showYesNoQuestion(editor, "How to Handle " + CONTRIB_BUNDLE_EXT,
                        "Install " + baseName + "?",
                        "Libraries, Modes, and Tools should<br>" +
                                "only be installed from trusted sources.");

        if (result == JOptionPane.YES_OPTION) {
          editor.statusNotice("Installing " + baseName + "...");
          editor.startIndeterminate();

          new Thread(() -> {
            try {
              LocalContribution contrib =
                      AvailableContribution.install(this, new File(path));

              EventQueue.invokeLater(() -> {
                editor.stopIndeterminate();
                if (contrib != null) {
                  editor.statusEmpty();
                } else {
                  editor.statusError("Could not install " + path);
                }
              });
            } catch (IOException e) {
              EventQueue.invokeLater(() ->
                      Messages.showWarning("Exception During Installation",
                              "Could not install contrib from " + path, e));
            }
          }).start();
        }
      }
    });
  }


  private boolean smellsLikeSketchFolder(File folder) {
    File[] files = folder.listFiles();
    if (files == null) {
      return false;
    }
    for (File file : files) {
      String name = file.getName();
      if (!(name.startsWith(".") ||
              name.toLowerCase().endsWith(".pde")) ||
              (file.isDirectory() && name.equals("data"))) {
        return false;
      }
    }
    return true;
  }


  private File moveLikeSketchFolder(File pdeFile, String baseName) throws IOException {
    Object[] options = { "Keep", "Move", "Cancel" };
    String prompt =
            "Would you like to keep \u201c" + pdeFile.getParentFile().getName() + "\u201d as the sketch folder,\n" +
                    "or move \u201c" + pdeFile.getName() + "\u201d to its own folder?\n" +
                    "(Usually, \u201c" + pdeFile.getName() + "\u201d would be stored inside a\n" +
                    "sketch folder named \u201c" + baseName + "\u201d.)";

    int result = JOptionPane.showOptionDialog(null, prompt, "Keep it? Move it?",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

    if (result == JOptionPane.YES_OPTION) {
      return pdeFile;
    } else if (result == JOptionPane.NO_OPTION) {
      File properFolder = new File(pdeFile.getParent(), baseName);
      if (properFolder.exists()) {
        throw new IOException("A folder named \"" + baseName + "\" " +
                "already exists. Cannot open sketch.");
      }
      if (!properFolder.mkdirs()) {
        throw new IOException("Could not create the sketch folder.");
      }
      File properPdeFile = new File(properFolder, pdeFile.getName());
      Util.copyFile(pdeFile, properPdeFile);
      if (!pdeFile.delete()) {
        Messages.err("Could not delete " + pdeFile);
      }
      return properPdeFile;
    }
    return null;
  }


  /**
   * Handler for pde:// protocol URIs
   */
  public Editor handleScheme(String schemeUri) {
    var result = Schema.handleSchema(schemeUri, this);
    if (result != null) {
      return result;
    }

    String location = schemeUri.substring(6);
    if (location.length() > 0) {
      if (location.charAt(0) == '/') {
        File file = new File(location);
        if (file.exists()) {
          handleOpen(location);
        } else {
          System.err.println(file + " does not exist.");
        }
      } else {
        final String url = "https://" + location;
        if (location.toLowerCase().endsWith(".pdez") ||
                location.toLowerCase().endsWith(".pdex")) {
          String extension = location.substring(location.length() - 5);
          try {
            File tempFile = File.createTempFile("scheme", extension);
            if (PApplet.saveStream(tempFile, Util.createInput(url))) {
              return handleOpen(tempFile.getAbsolutePath());
            } else {
              System.err.println("Could not open " + tempFile);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }


  /**
   * Open a sketch from the path specified. Do not use for untitled sketches.
   */
  public Editor handleOpen(String path) {
    if (path.startsWith("pde://")) {
      return handleScheme(path);
    }

    if (path.endsWith(SKETCH_BUNDLE_EXT)) {
      return openSketchBundle(path);
    } else if (path.endsWith(CONTRIB_BUNDLE_EXT)) {
      openContribBundle(path);
      return null;
    }

    File pdeFile = new File(path);
    if (!pdeFile.exists()) {
      System.err.println(path + " does not exist");
      return null;
    }

    for (Editor editor : editors) {
      for (SketchCode tab : editor.getSketch().getCode()) {
        if (tab.getFile().equals(pdeFile)) {
          editor.toFront();
          Recent.append(editor);
          return editor;
        }
      }
    }

    File parentFolder = pdeFile.getParentFile();

    try {
      Settings props = Sketch.loadProperties(parentFolder);
      if (!props.isEmpty()) {
        String modeIdentifier = props.get("mode.id");
        if (modeIdentifier != null) {
          if (modeIdentifier.equals("galsasson.mode.tweak.TweakMode")) {
            nextMode = getDefaultMode();
            props.remove("mode");
            props.remove("mode.id");
            props.reckon();
          } else {
            Mode mode = findMode(modeIdentifier);
            if (mode != null) {
              nextMode = mode;
            } else {
              ContributionManager.openModes();
              Messages.showWarning("Missing Mode",
                      "You must first install " + props.get("mode") + " Mode to use this sketch.");
              return null;
            }
          }
        }

        String main = props.get("main");
        if (main != null) {
          String mainPath = new File(parentFolder, main).getAbsolutePath();
          if (!path.equals(mainPath)) {
            System.out.println(path + " selected, but main is " + mainPath);
          }
          return handleOpenInternal(mainPath, false);
        }
      } else {
        nextMode = getDefaultMode();
      }

      if (!Sketch.isSanitaryName(pdeFile.getName())) {
        Messages.showWarning("You're tricky, but not tricky enough",
                pdeFile.getName() + " is not a valid name for sketch code.\n" +
                        "Better to stick to ASCII, no spaces, and make sure\n" +
                        "it doesn't start with a number.", null);
        return null;
      }

      String baseName = pdeFile.getName();
      int dot = baseName.lastIndexOf('.');
      if (dot == -1) {
        System.err.println(pdeFile + " does not have an extension.");
        return null;
      }
      baseName = baseName.substring(0, dot);
      if (!baseName.equals(parentFolder.getName())) {
        String filename =
                parentFolder.getName() + "." + nextMode.getDefaultExtension();
        File mainFile = new File(parentFolder, filename);
        if (mainFile.exists()) {
          pdeFile = mainFile;
        } else if (smellsLikeSketchFolder(parentFolder)) {
          props.set("main", pdeFile.getName());
          props.save();
        } else {
          File newFile = moveLikeSketchFolder(pdeFile, baseName);
          if (newFile == pdeFile) {
            props.set("main", newFile.getName());
            props.save();
          } else if (newFile == null) {
            return null;
          } else {
            pdeFile = newFile;
          }
        }
      }

      return handleOpenInternal(pdeFile.getAbsolutePath(), false);

    } catch (IOException e) {
      Messages.showWarning("sketch.properties",
              "Error while reading sketch.properties from\n" + parentFolder, e);
      return null;
    }
  }


  /**
   * Open a (vetted) sketch location using a particular Mode. Used by the
   * Examples window, because Modes like Python and Android do not have
   * "sketch.properties" files in each example folder.
   * The isExample flag prevents closing the initial untitled window,
   * since keeping it open as a reference can be useful when browsing examples.
   */
  public Editor handleOpenExample(String path, Mode mode) {
    nextMode = mode;
    return handleOpenInternal(path, true, true);  // isExample = true
  }


  /**
   * Open the sketch associated with this .pde file in a new window
   * as an "Untitled" sketch.
   */
  protected Editor handleOpenUntitled(String path) {
    return handleOpenInternal(path, true, false);
  }


  /**
   * Internal function to actually open the sketch. At this point, the
   * sketch file/folder must have been vetted, and nextMode set properly.
   */
  protected Editor handleOpenInternal(String path, boolean untitled) {
    return handleOpenInternal(path, untitled, false);
  }


  /**
   * Internal function to actually open the sketch.
   * @param path         Path to the sketch file
   * @param untitled     Whether this sketch is untitled
   * @param isExample    Whether this is being opened from the Examples browser.
   *                     If true, the initial untitled window is NOT closed,
   *                     so it can serve as a reference while browsing examples.
   *                     Fixes: https://github.com/processing/processing4/issues/1117
   */
  protected Editor handleOpenInternal(String path, boolean untitled, boolean isExample) {
    try {
      try {
        // [fix #1117] If a real sketch is being opened (not an example, not
        // a new untitled), and the only open editor is an untitled unmodified
        // window, close it first so it doesn't clutter the workspace.
        if (!isExample && !untitled && editors.size() == 1) {
          Editor existing = editors.get(0);
          if (existing.getSketch().isUntitled() &&
                  !existing.getSketch().isModified()) {
            Messages.log("Closing initial untitled window before opening: " + path);
            existing.setVisible(false);
            existing.dispose();
            editors.remove(existing);
            activeEditor = null;
          }
        }

        EditorState state = EditorState.nextEditor(editors);
        Editor editor = nextMode.createEditor(this, path, state);

        editor.setUpdatesAvailable(updatesAvailable);
        editor.getSketch().setUntitled(untitled);
        editors.add(editor);
        Recent.append(editor);

        editor.setVisible(true);

        return editor;

      } catch (EditorException ee) {
        if (ee.getMessage() != null) {
          Messages.showWarning("Error opening sketch", ee.getMessage(), ee);
        }
      } catch (NoSuchMethodError me) {
        Messages.showWarning("Mode out of date",
                nextMode.getTitle() + " is not compatible with this version of Processing.\n" +
                        "Try updating the Mode or contact its author for a new version.", me);
      } catch (Throwable t) {
        if (nextMode.equals(getDefaultMode())) {
          Messages.showTrace("Serious Problem",
                  "An unexpected, unknown, and unrecoverable error occurred\n" +
                          "while opening a new editor window. Please report this.", t, true);
        } else {
          Messages.showTrace("Mode Problems",
                  "A nasty error occurred while trying to use \u201c" + nextMode.getTitle() + "\u201d.\n" +
                          "It may not be compatible with this version of Processing.\n" +
                          "Try updating the Mode or contact its author for a new version.", t, false);
        }
      }
      if (editors.isEmpty()) {
        Mode defaultMode = getDefaultMode();
        if (nextMode == defaultMode) {
          Messages.showError("Editor Problems", """
              An error occurred while trying to change modes.
              We'll have to quit for now because it's an
              unfortunate bit of indigestion with the default Mode.
              """, null);
        } else {
          if (untitled) {
            nextMode = defaultMode;
            handleNew();
            return null;
          } else {
            return null;
          }
        }
      }
    } catch (Throwable t) {
      Messages.showTrace("Terrible News",
              "A serious error occurred while " +
                      "trying to create a new editor window.", t,
              nextMode == getDefaultMode());
      nextMode = getDefaultMode();
    }
    return null;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public boolean handleClose(Editor editor, boolean preventQuit) {
    if (!editor.checkModified()) {
      return false;
    }

    editor.internalCloseRunner();

    if (editors.size() == 1) {
      if (Platform.isMacOS()) {
        if (defaultFileMenu == null) {
          Object[] options = { Language.text("prompt.ok"), Language.text("prompt.cancel") };
          int result = JOptionPane.showOptionDialog(editor,
                  Toolkit.formatMessage("Are you sure you want to Quit?",
                          "Closing the last open sketch will quit Processing."),
                  "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                  null, options, options[0]);
          if (result == JOptionPane.NO_OPTION ||
                  result == JOptionPane.CLOSED_OPTION) {
            return false;
          }
        }
      }

      if (defaultFileMenu == null) {
        if (preventQuit) {
          editor.setVisible(false);
          editor.dispose();
          activeEditor = null;
          editors.remove(editor);
        } else {
          System.exit(0);
        }
      } else {
        editor.setVisible(false);
        editor.dispose();
        defaultFileMenu.insert(Recent.getMenu(), 2);
        activeEditor = null;
        editors.remove(editor);
      }

    } else {
      editor.setVisible(false);
      editor.dispose();
      editors.remove(editor);
    }
    return true;
  }


  public boolean handleQuit() {
    if (handleQuitEach()) {
      for (Editor editor : editors) {
        editor.internalCloseRunner();
      }
      Preferences.save();
      Console.shutdown();
      if (!Platform.isMacOS()) {
        System.exit(0);
      }
      return true;
    }
    return false;
  }


  protected boolean handleQuitEach() {
    for (Editor editor : editors) {
      if (!editor.checkModified()) {
        return false;
      }
    }
    return true;
  }


  public void handleRestart() {
    File app = Platform.getProcessingApp();
    System.out.println(app);
    if (app.exists()) {
      if (handleQuitEach()) {
        SingleInstance.clearRunning();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          try {
            System.out.println("launching");
            Process p;
            if (Platform.isMacOS()) {
              p = Runtime.getRuntime().exec(new String[] {
                      "open", "-n", "-a", app.getAbsolutePath()
              });
            } else if (Platform.isLinux()) {
              p = Runtime.getRuntime().exec(new String[] {
                      app.getAbsolutePath()
              });
            } else {
              p = Runtime.getRuntime().exec(new String[] {
                      "cmd", "/c", app.getAbsolutePath()
              });
            }
            System.out.println("launched with result " + p.waitFor());
            System.out.flush();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }));
        handleQuit();
        if (Platform.isMacOS()) {
          System.exit(0);
        }
      }
    } else {
      Messages.showWarning("Cannot Restart",
              "Cannot automatically restart because the Processing\n" +
                      "application has been renamed. Please quit and then restart manually.");
    }
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public void showExamplesFrame() {
    nextMode.showExamplesFrame();
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  protected SketchbookFrame sketchbookFrame;

  public DefaultMutableTreeNode buildSketchbookTree() {
    DefaultMutableTreeNode sbNode =
            new DefaultMutableTreeNode(Language.text("sketchbook.tree"));
    try {
      addSketches(sbNode, Base.getSketchbookFolder(), false);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sbNode;
  }

  public void rebuildSketchbookFrame() {
    if (sketchbookFrame != null) {
      sketchbookFrame.rebuild();
    }
  }

  public void showSketchbookFrame() {
    if (sketchbookFrame == null) {
      sketchbookFrame = new SketchbookFrame(this);
    }
    sketchbookFrame.setVisible();
  }

  public void rebuildSketchbook() {
    for (Mode mode : getModeList()) {
      mode.rebuildImportMenu();
      mode.rebuildToolbarMenu();
      mode.rebuildExamplesFrame();
    }
    rebuildSketchbookFrame();
  }


  public void populateSketchbookMenu(JMenu menu) {
    new Thread(() -> {
      boolean found = false;
      try {
        found = addSketches(menu, getSketchbookFolder());
      } catch (Exception e) {
        Messages.showWarning("Sketchbook Menu Error",
                "An error occurred while trying to list the sketchbook.", e);
      }
      if (!found) {
        JMenuItem empty = new JMenuItem(Language.text("menu.file.sketchbook.empty"));
        empty.setEnabled(false);
        menu.add(empty);
      }
    }).start();
  }


  protected boolean addSketches(JMenu menu, File folder) {
    Messages.log("scanning " + folder.getAbsolutePath());
    if (!folder.isDirectory()) return false;
    if (folder.getName().equals("android")) return false;
    if (folder.getName().equals("libraries")) return false;

    if (folder.getName().equals("sdk")) {
      File suspectSDKPath = new File(folder.getParent(), folder.getName());
      File expectedSDKPath = new File(getSketchbookFolder(), "android" + File.separator + "sdk");
      if (expectedSDKPath.getAbsolutePath().equals(suspectSDKPath.getAbsolutePath())) {
        return false;
      }
    }

    String[] list = folder.list();
    if (list == null) return false;
    Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);

    ActionListener listener = e -> {
      String path = e.getActionCommand();
      if (new File(path).exists()) {
        handleOpen(path);
      } else {
        Messages.showWarning("Sketch Disappeared", """
            The selected sketch no longer exists.
            You may need to restart Processing to update
            the sketchbook menu.""", null);
      }
    };

    boolean found = false;
    for (String name : list) {
      if (name.charAt(0) == '.') continue;
      if (name.equals("old")) continue;

      File entry = new File(folder, name);
      File sketchFile = null;
      if (entry.isDirectory()) {
        sketchFile = Sketch.findMain(entry, getModeList());
      } else if (name.toLowerCase().endsWith(SKETCH_BUNDLE_EXT)) {
        name = name.substring(0, name.length() - SKETCH_BUNDLE_EXT.length());
        sketchFile = entry;
      }

      if (sketchFile != null) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(listener);
        item.setActionCommand(sketchFile.getAbsolutePath());
        menu.add(item);
        found = true;
      } else if (entry.isDirectory()) {
        JMenu submenu = new JMenu(name);
        boolean anything = addSketches(submenu, entry);
        if (anything) {
          menu.add(submenu);
          found = true;
        }
      }
    }
    return found;
  }


  public boolean addSketches(DefaultMutableTreeNode node, File folder,
                             boolean examples) throws IOException {
    Messages.log("scanning " + folder.getAbsolutePath());
    if (!folder.isDirectory()) return false;

    final String folderName = folder.getName();
    if (folderName.equals("android")) return false;
    if (folderName.equals("libraries")) return false;
    if (!examples && folderName.equals("examples")) return false;

    String[] fileList = folder.list();
    if (fileList == null) return false;
    Arrays.sort(fileList, String.CASE_INSENSITIVE_ORDER);

    boolean found = false;
    for (String name : fileList) {
      if (name.charAt(0) == '.') continue;

      File entry = new File(folder, name);
      File sketchFile = null;
      if (entry.isDirectory()) {
        sketchFile = Sketch.findMain(entry, getModeList());
      } else if (name.toLowerCase().endsWith(SKETCH_BUNDLE_EXT)) {
        name = name.substring(0, name.length() - SKETCH_BUNDLE_EXT.length());
        sketchFile = entry;
      }

      if (sketchFile != null) {
        DefaultMutableTreeNode item =
                new DefaultMutableTreeNode(new SketchReference(name, sketchFile));
        node.add(item);
        found = true;
      } else if (entry.isDirectory()) {
        DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(name);
        boolean anything = addSketches(subNode, entry, examples);
        if (anything) {
          node.add(subNode);
          found = true;
        }
      }
    }
    return found;
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  public void handlePrefs() {
    PDEPreferencesKt.show();
  }

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


  @SuppressWarnings("RedundantThrows")
  static public File getLibFile(String filename) throws IOException {
    return new File(Platform.getContentFile("lib"), filename);
  }

  static public InputStream getLibStream(String filename) throws IOException {
    return new FileInputStream(getLibFile(filename));
  }


  /**
   * @deprecated use processing.utils.Settings.getFolder() instead
   */
  static public File getSettingsFolder() {
    var override = getSettingsOverride();
    if (override != null) {
      return override;
    }
    try {
      return processing.utils.Settings.getFolder();
    } catch (processing.utils.Settings.SettingsFolderException e) {
      switch (e.getType()) {
        case COULD_NOT_CREATE_FOLDER -> Messages.showError("Settings issues",
                "Processing cannot run because it could not\n" +
                        "create a folder to store your settings at\n" + e.getMessage(), null);
        case WINDOWS_APPDATA_NOT_FOUND -> Messages.showError("Settings issues",
                "Processing cannot run because it could not\n" +
                        "find the AppData or LocalAppData folder on your system.", null);
        case MACOS_LIBRARY_FOLDER_NOT_FOUND -> Messages.showError("Settings issues",
                "Processing cannot run because it could not\n" +
                        "find the Library folder on your system.", null);
        case LINUX_CONFIG_FOLDER_NOT_FOUND -> Messages.showError("Settings issues",
                "Processing cannot run because either your\n" +
                        "XDG_CONFIG_HOME or SNAP_USER_COMMON is set\n" +
                        "but the folder does not exist.", null);
        case LINUX_SUDO_USER_ERROR -> Messages.showError("Settings issues",
                "Processing cannot run because it was started\n" +
                        "with sudo and Processing could not resolve\n" +
                        "the original users home directory.", null);
        default -> Messages.showTrace("An rare and unknowable thing happened",
                "Could not get the settings folder. Please report:\n" +
                        "http://github.com/processing/processing4/issues/new", e, true);
      }
    }
    throw new RuntimeException("Unreachable code in Base.getSettingsFolder()");
  }


  static public File getSettingsOverride() {
    return settingsOverride;
  }

  static public File getSettingsFile(String filename) {
    return new File(getSettingsFolder(), filename);
  }

  static public File getToolsFolder() {
    return Platform.getContentFile("tools");
  }

  static protected File sketchbookFolder;

  static public void locateSketchbookFolder() {
    String sketchbookPath = Preferences.getSketchbookPath();
    if (sketchbookPath != null) {
      sketchbookFolder = new File(sketchbookPath);
      if (!sketchbookFolder.exists()) {
        Messages.showWarning("Sketchbook folder disappeared", """
            The sketchbook folder no longer exists.
            Processing will switch to the default sketchbook
            location, and create a new sketchbook folder if
            necessary. Processing will then stop talking
            about itself in the third person.""", null);
        sketchbookFolder = null;
      }
    }

    if (sketchbookFolder == null) {
      sketchbookFolder = getDefaultSketchbookFolder();
      Preferences.setSketchbookPath(sketchbookFolder.getAbsolutePath());
      if (!sketchbookFolder.exists()) {
        if (!sketchbookFolder.mkdirs()) {
          Messages.showError("Could not create sketchbook",
                  "Unable to create a sketchbook folder at\n" +
                          sketchbookFolder + "\n" +
                          "Try creating a folder at that path and restart Processing.", null);
        }
      }
    }
    makeSketchbookSubfolders();
  }


  public void setSketchbookFolder(File folder) {
    sketchbookFolder = folder;
    Preferences.setSketchbookPath(folder.getAbsolutePath());
    rebuildSketchbook();
    makeSketchbookSubfolders();
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  static protected void makeSketchbookSubfolders() {
    getSketchbookLibrariesFolder().mkdirs();
    getSketchbookToolsFolder().mkdirs();
    getSketchbookModesFolder().mkdirs();
    getSketchbookExamplesFolder().mkdirs();
    getSketchbookTemplatesFolder().mkdirs();
  }


  static public File getSketchbookFolder() {
    var sketchbookPathOverride = System.getProperty("processing.sketchbook.folder");
    if (sketchbookPathOverride != null && !sketchbookPathOverride.isEmpty()) {
      return new File(sketchbookPathOverride);
    }
    if (sketchbookFolder == null) {
      locateSketchbookFolder();
    }
    return sketchbookFolder;
  }

  static public File getSketchbookLibrariesFolder() {
    return new File(getSketchbookFolder(), "libraries");
  }

  static public File getSketchbookToolsFolder() {
    return new File(getSketchbookFolder(), "tools");
  }

  static public File getSketchbookModesFolder() {
    return new File(getSketchbookFolder(), "modes");
  }

  static public File getSketchbookExamplesFolder() {
    return new File(getSketchbookFolder(), "examples");
  }

  static public File getSketchbookTemplatesFolder() {
    return new File(getSketchbookFolder(), "templates");
  }


  @NotNull
  static protected File getDefaultSketchbookFolder() {
    File sketchbookFolder = null;
    try {
      sketchbookFolder = Platform.getDefaultSketchbookFolder();
    } catch (Exception ignored) { }

    if (sketchbookFolder == null) {
      Messages.showError("No sketchbook",
              "Problem while trying to get the sketchbook", null);
    } else {
      boolean result = true;
      if (!sketchbookFolder.exists()) {
        result = sketchbookFolder.mkdirs();
      }
      if (!result) {
        Messages.showError("You forgot your sketchbook",
                "Processing cannot run because it could not\n" +
                        "create a folder to store your sketchbook.", null);
      }
    }
    return sketchbookFolder;
  }
}