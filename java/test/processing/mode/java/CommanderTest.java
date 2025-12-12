package processing.mode.java;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import processing.app.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;


public class CommanderTest {

  private File tempSketchFolder;

  @Before
  public void setUp() throws IOException {
    tempSketchFolder = Files.createTempDirectory("sketch_test").toFile();
  }

  @After
  public void tearDown() {
    if (tempSketchFolder != null && tempSketchFolder.exists()) {
      deleteDirectory(tempSketchFolder);
    }
  }

  @Test
  public void testSketchWithDefaultMainFile() throws IOException {
    String sketchName = tempSketchFolder.getName();
    File mainFile = new File(tempSketchFolder, sketchName + ".pde");
    
    try (FileWriter writer = new FileWriter(mainFile)) {
      writer.write("void setup() {}\nvoid draw() {}");
    }

    assertTrue("Default main file should exist", mainFile.exists());
  }

  @Test
  public void testSketchWithCustomMainFile() throws IOException {
    File customMainFile = new File(tempSketchFolder, "custom_main.pde");
    try (FileWriter writer = new FileWriter(customMainFile)) {
      writer.write("void setup() {}\nvoid draw() {}");
    }

    File propsFile = new File(tempSketchFolder, "sketch.properties");
    Settings props = new Settings(propsFile);
    props.set("main", "custom_main.pde");
    props.save();

    assertTrue("Custom main file should exist", customMainFile.exists());
    assertTrue("sketch.properties should exist", propsFile.exists());

    Settings readProps = new Settings(propsFile);
    assertEquals("custom_main.pde", readProps.get("main"));
  }

  @Test
  public void testSketchPropertiesMainProperty() throws IOException {
    File propsFile = new File(tempSketchFolder, "sketch.properties");
    Settings props = new Settings(propsFile);
    props.set("main", "my_sketch.pde");
    props.save();

    Settings readProps = new Settings(propsFile);
    String mainFile = readProps.get("main");
    
    assertEquals("Main property should match", "my_sketch.pde", mainFile);
  }

  private void deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    directory.delete();
  }
}
