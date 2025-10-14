package processing.core;

import org.junit.Assert;
import org.junit.Test;
import processing.data.XML;

public class PShapeSVGPathTest {

  @Test
  public void testCompactPathNotation() {
    // Test the failing SVG from issue #1244
    String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" viewBox=\"0 0 29 29\">" +
      "<path d=\"m0 6 3-2 15 4 7-7a2 2 0 013 3l-7 7 4 15-2 3-7-13-5 5v4l-2 2-2-5-5-2 2-2h4l5-5z\"/>" +
      "</svg>";
    
    try {
      XML xml = XML.parse(svgContent);
      PShapeSVG shape = new PShapeSVG(xml);
      Assert.assertNotNull(shape);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
  }
  
  @Test
  public void testWorkingPathNotation() {
    // Test the working SVG (with explicit decimal points)
    String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" viewBox=\"0 0 29 29\">" +
      "<path d=\"m 0,5.9994379 2.9997,-1.9998 14.9985,3.9996 6.9993,-6.99930004 a 2.1211082,2.1211082 0 0 1 2.9997,2.99970004 l -6.9993,6.9993001 3.9996,14.9985 -1.9998,2.9997 -6.9993,-12.9987 -4.9995,4.9995 v 3.9996 l -1.9998,1.9998 -1.9998,-4.9995 -4.9995,-1.9998 1.9998,-1.9998 h 3.9996 l 4.9995,-4.9995 z\"/>" +
      "</svg>";
    
    try {
      XML xml = XML.parse(svgContent);
      PShapeSVG shape = new PShapeSVG(xml);
      Assert.assertNotNull(shape);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
  }
  
  @Test
  public void testCompactArcNotationVariations() {
    // Test various compact arc notations
    String[] testCases = {
      // Flags only concatenated (e.g., "01")
      "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><path d=\"M10 10 A30 30 0 0110 50\"/></svg>",
      // Flags and coordinate concatenated (e.g., "013")
      "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><path d=\"M10 10 A30 30 0 013 50\"/></svg>",
      // Standard notation (should still work)
      "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><path d=\"M10 10 A30 30 0 0 1 10 50\"/></svg>"
    };
    
    try {
      for (String svgContent : testCases) {
        XML xml = XML.parse(svgContent);
        PShapeSVG shape = new PShapeSVG(xml);
        Assert.assertNotNull(shape);
      }
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
  }
  
  @Test
  public void testCompactArcWithNegativeCoordinates() {
    // Test compact arc notation with negative coordinates
    String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">" +
      "<path d=\"M50 50 a20 20 0 01-10 20\"/>" +
      "</svg>";
    
    try {
      XML xml = XML.parse(svgContent);
      PShapeSVG shape = new PShapeSVG(xml);
      Assert.assertNotNull(shape);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
  }
}
