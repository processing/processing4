package processing.core;

import org.junit.Assert;
import org.junit.Test;
import processing.data.XML;

public class PShapeSVGPathTest {

  @Test
  public void testCompactPathNotation() {
    String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" viewBox=\"0 0 29 29\">" +
      "<path d=\"m0 6 3-2 15 4 7-7a2 2 0 013 3l-7 7 4 15-2 3-7-13-5 5v4l-2 2-2-5-5-2 2-2h4l5-5z\"/>" +
      "</svg>";
    
    try {
      XML xml = XML.parse(svgContent);
      PShapeSVG shape = new PShapeSVG(xml);
      Assert.assertNotNull(shape);
      Assert.assertTrue(shape.getChildCount() > 0);
      
      PShape path = shape.getChild(0);
      Assert.assertNotNull(path);
      Assert.assertTrue(path.getVertexCount() > 5);
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
    String svgContent1 = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">" +
      "<path d=\"M10 10 A30 30 0 013 50\"/></svg>";
    
    try {
      XML xml = XML.parse(svgContent1);
      PShapeSVG shape = new PShapeSVG(xml);
      PShape path = shape.getChild(0);
      int vertexCount = path.getVertexCount();
      PVector lastVertex = path.getVertex(vertexCount - 1);
      Assert.assertEquals(3.0f, lastVertex.x, 0.0001f);
      Assert.assertEquals(50.0f, lastVertex.y, 0.0001f);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
    
    String svgContent2 = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">" +
      "<path d=\"M10 10 A30 30 0 0110 50\"/></svg>";
    
    try {
      XML xml = XML.parse(svgContent2);
      PShapeSVG shape = new PShapeSVG(xml);
      PShape path = shape.getChild(0);
      int vertexCount = path.getVertexCount();
      PVector lastVertex = path.getVertex(vertexCount - 1);
      Assert.assertEquals(10.0f, lastVertex.x, 0.0001f);
      Assert.assertEquals(50.0f, lastVertex.y, 0.0001f);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
    
    String svgContent3 = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">" +
      "<path d=\"M10 10 A30 30 0 0 1 10 50\"/></svg>";
    
    try {
      XML xml = XML.parse(svgContent3);
      PShapeSVG shape = new PShapeSVG(xml);
      PShape path = shape.getChild(0);
      int vertexCount = path.getVertexCount();
      PVector lastVertex = path.getVertex(vertexCount - 1);
      Assert.assertEquals(10.0f, lastVertex.x, 0.0001f);
      Assert.assertEquals(50.0f, lastVertex.y, 0.0001f);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
  }
  
  @Test
  public void testCompactArcWithNegativeCoordinates() {
    String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">" +
      "<path d=\"M50 50 a20 20 0 01-10 20\"/></svg>";
    
    try {
      XML xml = XML.parse(svgContent);
      PShapeSVG shape = new PShapeSVG(xml);
      PShape path = shape.getChild(0);
      int vertexCount = path.getVertexCount();
      PVector lastVertex = path.getVertex(vertexCount - 1);
      Assert.assertEquals(40.0f, lastVertex.x, 0.0001f);
      Assert.assertEquals(70.0f, lastVertex.y, 0.0001f);
    } catch (Exception e) {
      Assert.fail("Encountered exception " + e);
    }
  }
}
