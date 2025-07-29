package processing.core;

import org.junit.Assert;
import org.junit.Test;

public class PVectorTest {

  @Test
  public void testConstructors() {
    PVector v0 = new PVector();
    Assert.assertEquals(0, v0.x, 0.0001f);
    Assert.assertEquals(0, v0.y, 0.0001f);
    Assert.assertEquals(0, v0.z, 0.0001f);

    PVector v2 = new PVector(3, 4);
    Assert.assertEquals(3, v2.x, 0.0001f);
    Assert.assertEquals(4, v2.y, 0.0001f);
    Assert.assertEquals(0, v2.z, 0.0001f);

    PVector v3 = new PVector(1, 2, 3);
    Assert.assertEquals(1, v3.x, 0.0001f);
    Assert.assertEquals(2, v3.y, 0.0001f);
    Assert.assertEquals(3, v3.z, 0.0001f);
  }

  @Test
  public void testSetAndCopy() {
    PVector v = new PVector(1, 2, 3);
    PVector copy = v.copy();
    Assert.assertEquals(v.x, copy.x, 0.0001f);
    Assert.assertEquals(v.y, copy.y, 0.0001f);
    Assert.assertEquals(v.z, copy.z, 0.0001f);
    
    v.set(4, 5, 6);
    Assert.assertEquals(4, v.x, 0.0001f);
    Assert.assertEquals(5, v.y, 0.0001f);
    Assert.assertEquals(6, v.z, 0.0001f);
  }

  @Test
  public void testAdd() {
    PVector v1 = new PVector(1, 1, 1);
    PVector v2 = new PVector(2, 3, 4);
    v1.add(v2);
    Assert.assertEquals(3, v1.x, 0.0001f);
    Assert.assertEquals(4, v1.y, 0.0001f);
    Assert.assertEquals(5, v1.z, 0.0001f);

    PVector v3 = new PVector(1, 2, 3);
    PVector result = PVector.add(v3, new PVector(4, 5, 6));
    Assert.assertEquals(5, result.x, 0.0001f);
    Assert.assertEquals(7, result.y, 0.0001f);
    Assert.assertEquals(9, result.z, 0.0001f);
  }

  @Test
  public void testSub() {
    PVector v1 = new PVector(5, 7, 9);
    PVector v2 = new PVector(1, 2, 3);
    v1.sub(v2);
    Assert.assertEquals(4, v1.x, 0.0001f);
    Assert.assertEquals(5, v1.y, 0.0001f);
    Assert.assertEquals(6, v1.z, 0.0001f);

    PVector v3 = new PVector(10, 10, 10);
    PVector result = PVector.sub(v3, new PVector(3, 3, 3));
    Assert.assertEquals(7, result.x, 0.0001f);
    Assert.assertEquals(7, result.y, 0.0001f);
    Assert.assertEquals(7, result.z, 0.0001f);
  }

  @Test
  public void testMult() {
    PVector v = new PVector(1, 2, 3);
    v.mult(2);
    Assert.assertEquals(2, v.x, 0.0001f);
    Assert.assertEquals(4, v.y, 0.0001f);
    Assert.assertEquals(6, v.z, 0.0001f);

    PVector result = PVector.mult(new PVector(1, 1, 1), 5);
    Assert.assertEquals(5, result.x, 0.0001f);
    Assert.assertEquals(5, result.y, 0.0001f);
    Assert.assertEquals(5, result.z, 0.0001f);
  }

  @Test
  public void testDiv() {
    PVector v1 = new PVector(10, 20, 30);
    v1.div(2);
    Assert.assertEquals(5, v1.x, 0.0001f);
    Assert.assertEquals(10, v1.y, 0.0001f);
    Assert.assertEquals(15, v1.z, 0.0001f);

    PVector result = PVector.div(new PVector(10, 20, 30), 2);
    Assert.assertEquals(5, result.x, 0.0001f);
    Assert.assertEquals(10, result.y, 0.0001f);
    Assert.assertEquals(15, result.z, 0.0001f);

    // Division by zero
    PVector v2 = new PVector(1, 2, 3);
    v2.div(0);
    Assert.assertTrue(Float.isInfinite(v2.x));
    Assert.assertTrue(Float.isInfinite(v2.y));
    Assert.assertTrue(Float.isInfinite(v2.z));
  }

  @Test
  public void testMagnitude() {
    PVector v = new PVector(3, 4, 0);
    Assert.assertEquals(5, v.mag(), 0.0001f);
    Assert.assertEquals(25, v.magSq(), 0.0001f);
  }

  @Test
  public void testDot() {
    PVector v1 = new PVector(1, 2, 3);
    PVector v2 = new PVector(4, -5, 6);
    float dot = v1.dot(v2);
    Assert.assertEquals(12, dot, 0.0001f);

    float dotStatic = PVector.dot(v1, v2);
    Assert.assertEquals(12, dotStatic, 0.0001f);
  }

  @Test
  public void testCross() {
    PVector v1 = new PVector(1, 0, 0);
    PVector v2 = new PVector(0, 1, 0);
    PVector cross = v1.cross(v2);
    Assert.assertEquals(0, cross.x, 0.0001f);
    Assert.assertEquals(0, cross.y, 0.0001f);
    Assert.assertEquals(1, cross.z, 0.0001f);
  }

  @Test
  public void testNormalize() {
    PVector v1 = new PVector(3, 4, 0);
    v1.normalize();
    Assert.assertEquals(1, v1.mag(), 0.0001f);

    //with target
    PVector v2 = new PVector(3, 4, 0);
    PVector target = new PVector();
    PVector result = v2.normalize(target);
    Assert.assertSame(target, result);
    Assert.assertEquals(0.6f, result.x, 0.0001f);
    Assert.assertEquals(0.8f, result.y, 0.0001f);
    Assert.assertEquals(0, result.z, 0.0001f);

    // Normalize zero vector
    PVector zero = new PVector(0, 0, 0);
    zero.normalize();
    Assert.assertEquals(0, zero.x, 0.0001f);
    Assert.assertEquals(0, zero.y, 0.0001f);
    Assert.assertEquals(0, zero.z, 0.0001f);

  }

  @Test
  public void testLimit() {
    PVector v = new PVector(10, 0, 0);
    v.limit(5);
    Assert.assertEquals(5, v.mag(), 0.0001f);
  }

  @Test
  public void testSetMag() {
    PVector v = new PVector(3, 4, 0);
    v.setMag(10);
    Assert.assertEquals(10, v.mag(), 0.0001f);
  }

  @Test
  public void testHeading() {
    PVector v = new PVector(0, 1);
    float heading = v.heading();
    Assert.assertEquals(PConstants.HALF_PI, heading, 0.0001f);
  }

  @Test
  public void testRotate() {
    PVector v = new PVector(1, 0);
    v.rotate(PConstants.HALF_PI);
    Assert.assertEquals(0, v.x, 0.0001f);
    Assert.assertEquals(1, v.y, 0.0001f);
  }

  @Test
  public void testLerp() {
    PVector v1 = new PVector(0, 0, 0);
    PVector v2 = new PVector(10, 10, 10);
    v1.lerp(v2, 0.5f);
    Assert.assertEquals(5, v1.x, 0.0001f);
    Assert.assertEquals(5, v1.y, 0.0001f);
    Assert.assertEquals(5, v1.z, 0.0001f);

    PVector result = PVector.lerp(new PVector(0, 0, 0), new PVector(10, 10, 10), 0.5f);
    Assert.assertEquals(5, result.x, 0.0001f);
    Assert.assertEquals(5, result.y, 0.0001f);
    Assert.assertEquals(5, result.z, 0.0001f);
  }

  @Test
  public void testAngleBetween() {
    PVector v1 = new PVector(1, 0, 0);
    PVector v2 = new PVector(0, 1, 0);
    float a1 = PVector.angleBetween(v1, v2);
    Assert.assertEquals(PConstants.HALF_PI, a1, 0.0001f);

    // angleBetween with zero vectors
    float a2 = PVector.angleBetween(new PVector(0, 0, 0), new PVector(1, 0, 0));
    Assert.assertEquals(0, a2, 0.0001f);  

    // angleBetween with parallel vectors
    float a3 = PVector.angleBetween(new PVector(1, 0, 0), new PVector(2, 0, 0));
    Assert.assertEquals(0, a3, 0.0001f);
    
    // angleBetween with opposite vectors
    float a4 = PVector.angleBetween(new PVector(1, 0, 0), new PVector(-1, 0, 0));
    Assert.assertEquals(PConstants.PI, a4, 0.0001f);
  }

  @Test
  public void testFromAngle() {
    PVector v = PVector.fromAngle(0);
    Assert.assertEquals(1, v.x, 0.0001f);
    Assert.assertEquals(0, v.y, 0.0001f);
    Assert.assertEquals(0, v.z, 0.0001f);
    
    v = PVector.fromAngle(PConstants.HALF_PI);
    Assert.assertEquals(0, v.x, 0.0001f);
    Assert.assertEquals(1, v.y, 0.0001f);
    Assert.assertEquals(0, v.z, 0.0001f);
    
    PVector target = new PVector();
    PVector result = PVector.fromAngle(PConstants.PI, target);
    Assert.assertSame(target, result);
    Assert.assertEquals(-1, result.x, 0.0001f);
    Assert.assertEquals(0, result.y, 0.0001f);
  }


  @Test
  public void testArray() {
    PVector v = new PVector(3, 4, 5);
    float[] arr = v.array();
    Assert.assertEquals(3, arr[0], 0.0001f);
    Assert.assertEquals(4, arr[1], 0.0001f);
    Assert.assertEquals(5, arr[2], 0.0001f);
  }

  @Test
  public void testRandom2D() {
    PVector v = PVector.random2D();
    Assert.assertEquals(1, v.mag(), 0.0001f); 
    Assert.assertEquals(0, v.z, 0.0001f); 
    
    PVector target = new PVector();
    PVector result = PVector.random2D(target);
    Assert.assertSame(target, result); 
    Assert.assertEquals(1, result.mag(), 0.0001f);
  }
  
  @Test
  public void testRandom3D() {
    PVector v = PVector.random3D();
    Assert.assertEquals(1, v.mag(), 0.0001f); 
    
    PVector target = new PVector();
    PVector result = PVector.random3D(target);
    Assert.assertSame(target, result); 
    Assert.assertEquals(1, result.mag(), 0.0001f);
  }
  

  @Test
  public void testEqualsAndHashCode() {
    PVector v1 = new PVector(1, 2, 3);
    PVector v2 = new PVector(1, 2, 3);
    PVector v3 = new PVector(3, 2, 1);

    Assert.assertTrue(v1.equals(v2));
    Assert.assertFalse(v1.equals(v3));
    Assert.assertEquals(v1.hashCode(), v2.hashCode());
  }

  @Test
  public void testToString() {
    PVector v = new PVector(1, 2, 3);
    String expected = "[ 1.0, 2.0, 3.0 ]";
    Assert.assertEquals(expected, v.toString());
  }

}
