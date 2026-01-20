package processing.core;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for pixel density scaling functionality, including logical pixel arrays
 * and pixel access modes (PIXEL_EXACT vs PIXEL_SMOOTH).
 *
 * TODO:
 *  - Fractional scaling.
 *  - Some kind of openGL test.
 */
public class PixelDensityTest {
  
  private TestPApplet sketch;
  private PGraphics java2d;
  private PGraphics opengl;
  
  static class TestPApplet extends PApplet {
    public void settings() {
      size(100, 100);
    }
    
    public void setup() {
    }
    
    public void draw() {
    }
  }
  
  @Before
  public void setUp() {
    sketch = new TestPApplet();
    sketch.settings();
    sketch.setup();
    sketch.initSurface();
    
    // 2d graphics
    java2d = sketch.createGraphics(100, 100, PConstants.JAVA2D);
    
    try {
      opengl = sketch.createGraphics(100, 100, PConstants.P3D);
    } catch (Exception e) {
      // headless, ci, etc
      opengl = null;
    }
  }
  
  @Test
  public void testPixelDensityConstants() {
    assertEquals("PIXEL_EXACT should be 0", 0, PConstants.PIXEL_EXACT);
    assertEquals("PIXEL_SMOOTH should be 1", 1, PConstants.PIXEL_SMOOTH);
  }
  
  @Test
  public void testPixelAccessModeValidation() {
    sketch.pixelAccessMode(PConstants.PIXEL_EXACT);
    assertEquals(PConstants.PIXEL_EXACT, sketch.pixelAccessMode);
    
    sketch.pixelAccessMode(PConstants.PIXEL_SMOOTH);
    assertEquals(PConstants.PIXEL_SMOOTH, sketch.pixelAccessMode);
    
    try {
      sketch.pixelAccessMode(99);
      fail("Should throw exception for invalid mode");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("PIXEL_EXACT or PIXEL_SMOOTH"));
    }
  }
  
  @Test
  public void testLogicalPixelArraySize() {
    java2d.pixelDensity = 2;
    java2d.beginDraw();
    
    assertEquals("Logical width should be unchanged", 100, java2d.width);
    assertEquals("Logical height should be unchanged", 100, java2d.height);
    
    assertEquals("Physical width should be scaled", 200, java2d.pixelWidth);
    assertEquals("Physical height should be scaled", 200, java2d.pixelHeight);
    
    java2d.loadPixels();
    assertEquals("Pixels array should be logical size", 100 * 100, java2d.pixels.length);
    
    java2d.endDraw();
  }
  
  @Test
  public void testPixelExactModeSymmetry() {
    sketch.pixelAccessMode(PConstants.PIXEL_EXACT);
    
    java2d.pixelDensity = 2;
    java2d.beginDraw();
    java2d.background(0);
    
    java2d.set(10, 10, 0xFFFF0000); // Red
    int redColor = java2d.get(10, 10);
    java2d.set(10, 10, redColor);
    int finalColor = java2d.get(10, 10);

    assertEquals("set(get()) should be a no-op", redColor, finalColor);

    java2d.endDraw();
  }
  
  @Test
  public void testPixelSmoothModeSymmetry() {
    sketch.pixelAccessMode(PConstants.PIXEL_SMOOTH);
    
    java2d.pixelDensity = 2;
    java2d.beginDraw();
    java2d.background(0);

    java2d.set(10, 10, 0xFFFF0000); // Red
    int redColor = java2d.get(10, 10);
    java2d.set(10, 10, redColor);
    int finalColor = java2d.get(10, 10);

    int redDiff = Math.abs(((redColor >> 16) & 0xFF) - ((finalColor >> 16) & 0xFF));
    int greenDiff = Math.abs(((redColor >> 8) & 0xFF) - ((finalColor >> 8) & 0xFF));
    int blueDiff = Math.abs((redColor & 0xFF) - (finalColor & 0xFF));
    
    assertTrue("Red channel should be approximately equal", redDiff <= 2);
    assertTrue("Green channel should be approximately equal", greenDiff <= 2);
    assertTrue("Blue channel should be approximately equal", blueDiff <= 2);
    
    java2d.endDraw();
  }
  
  @Test
  public void testPixelCoordinatesInBounds() {
    java2d.pixelDensity = 2;
    java2d.beginDraw();
    
    for (int mode : new int[]{PConstants.PIXEL_EXACT, PConstants.PIXEL_SMOOTH}) {
      sketch.pixelAccessMode(mode);
      
      java2d.set(0, 0, 0xFFFF0000);
      java2d.set(99, 0, 0xFF00FF00);
      java2d.set(0, 99, 0xFF0000FF);
      java2d.set(99, 99, 0xFFFFFF00);
      
      assertEquals("Top-left should be red", 0xFFFF0000, java2d.get(0, 0));
      assertEquals("Top-right should be green", 0xFF00FF00, java2d.get(99, 0));
      assertEquals("Bottom-left should be blue", 0xFF0000FF, java2d.get(0, 99));
      assertEquals("Bottom-right should be yellow", 0xFFFFFF00, java2d.get(99, 99));
      
      assertEquals("Out of bounds should return 0", 0, java2d.get(-1, 50));
      assertEquals("Out of bounds should return 0", 0, java2d.get(50, -1));
      assertEquals("Out of bounds should return 0", 0, java2d.get(100, 50));
      assertEquals("Out of bounds should return 0", 0, java2d.get(50, 100));
    }
    
    java2d.endDraw();
  }
  
  @Test
  public void testPixelArrayOperations() {
    java2d.pixelDensity = 2;
    java2d.beginDraw();
    java2d.background(0);
    
    java2d.loadPixels();
    
    java2d.pixels[0] = 0xFFFF0000; // Top-left red
    java2d.pixels[99] = 0xFF00FF00; // Top-right green
    java2d.pixels[99 * 100] = 0xFF0000FF; // Bottom-left blue
    java2d.pixels[99 * 100 + 99] = 0xFFFFFF00; // Bottom-right yellow
    
    java2d.updatePixels();
    
    assertEquals("Array set should match get()", 0xFFFF0000, java2d.get(0, 0));
    assertEquals("Array set should match get()", 0xFF00FF00, java2d.get(99, 0));
    assertEquals("Array set should match get()", 0xFF0000FF, java2d.get(0, 99));
    assertEquals("Array set should match get()", 0xFFFFFF00, java2d.get(99, 99));
    
    java2d.endDraw();
  }
  
  @Test
  public void testOpenGLPixelDensity() {
    if (opengl == null) {
      System.out.println("Skipping OpenGL test - not available");
      return;
    }
    
    opengl.pixelDensity = 2;
    opengl.beginDraw();
    opengl.background(0);
    
    opengl.set(10, 10, 0xFFFF0000);
    int color = opengl.get(10, 10);
    
    int red = (color >> 16) & 0xFF;
    assertTrue("Red channel should be preserved", red > 200);
    
    opengl.endDraw();
  }
  
  @Test
  public void testBackwardCompatibility() {
    java2d.pixelDensity = 1;
    java2d.beginDraw();
    
    assertEquals("Logical and physical width should be equal", java2d.width, java2d.pixelWidth);
    assertEquals("Logical and physical height should be equal", java2d.height, java2d.pixelHeight);
    
    java2d.loadPixels();
    assertEquals("Pixels array should match physical size", java2d.pixelWidth * java2d.pixelHeight, java2d.pixels.length);
    
    java2d.set(50, 50, 0xFFFF0000);
    assertEquals("Get should return set value", 0xFFFF0000, java2d.get(50, 50));
    
    java2d.endDraw();
  }
}