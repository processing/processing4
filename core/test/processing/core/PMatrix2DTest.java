package processing.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PMatrix2DTest {

  private PMatrix2D m;

  @Before
  public void setUp() {
    m = new PMatrix2D();
  }

  @Test
  public void testIdentity() {
    assertTrue("New matrix should be identity", m.isIdentity());
    float[] arr = m.get(null);
    assertEquals(1, arr[0], 0.0001f);  // m00
    assertEquals(0, arr[1], 0.0001f);  // m01
    assertEquals(0, arr[2], 0.0001f);  // m02
    assertEquals(0, arr[3], 0.0001f);  // m10
    assertEquals(1, arr[4], 0.0001f);  // m11
    assertEquals(0, arr[5], 0.0001f);  // m12
  }

  @Test
  public void testTranslate() {
    m.translate(10, 20);
    assertEquals(10, m.m02, 0.0001f);
    assertEquals(20, m.m12, 0.0001f);
  }

  @Test
  public void testRotate() {
    m.rotate(PConstants.HALF_PI);
    assertEquals(0, m.m00, 0.0001f);
    assertEquals(-1, m.m01, 0.0001f); 
    assertEquals(1, m.m10, 0.0001f); 
    assertEquals(0, m.m11, 0.0001f);
  }


  @Test
  public void testScale() {
    m.scale(2, 3);
    assertEquals(2, m.m00, 0.0001f);
    assertEquals(3, m.m11, 0.0001f);
    assertEquals(0, m.m02, 0.0001f);
    assertEquals(0, m.m12, 0.0001f);
  }

  @Test
  public void testShear() {
      float shearAngle = 0.2f; 
      m.shearX(shearAngle);
      assertEquals(0, m.m01, 0.0001f); 
      assertEquals((float)Math.tan(shearAngle), m.m10, 0.0001f); 
      assertEquals(1, m.m02, 0.0001f);
  
      m.reset();
      
      m.shearY(shearAngle);
      assertEquals(0, m.m01, 0.0001f); 
      assertEquals(0, m.m10, 0.0001f);
      assertEquals((float)Math.tan(shearAngle), m.m11, 0.0001f);
      assertEquals(1, m.m02, 0.0001f); 
  }

  @Test
  public void testApply() {
    PMatrix2D m2 = new PMatrix2D(1, 2, 3, 4, 5, 6);
    m.apply(m2);
    assertEquals(m2.m00, m.m00, 0.0001f);
    assertEquals(m2.m01, m.m01, 0.0001f);
    assertEquals(m2.m02, m.m02, 0.0001f);
    assertEquals(m2.m10, m.m10, 0.0001f);
    assertEquals(m2.m11, m.m11, 0.0001f);
    assertEquals(m2.m12, m.m12, 0.0001f);
  }

  @Test
  public void testPreApply() {
    PMatrix2D m1 = new PMatrix2D(1, 2, 3, 4, 5, 6);
    m.reset(); // identity matrix
    m.preApply(m1);
    assertEquals(m1.m00, m.m00, 0.0001f);
    assertEquals(m1.m01, m.m01, 0.0001f);
    assertEquals(m1.m02, m.m02, 0.0001f);
    assertEquals(m1.m10, m.m10, 0.0001f);
    assertEquals(m1.m11, m.m11, 0.0001f);
    assertEquals(m1.m12, m.m12, 0.0001f);
  }

  @Test
  public void testMultPVector() {
    PVector src = new PVector(1, 2, 0);
    PVector result = m.mult(src, null);
    assertEquals(src.x, result.x, 0.0001f);
    assertEquals(src.y, result.y, 0.0001f);
  }

  @Test
  public void testMultArray() {
    float[] vec = { 1, 2 };
    float[] out = m.mult(vec, null);
    assertEquals(1, out[0], 0.0001f);
    assertEquals(2, out[1], 0.0001f);
  }

  @Test
  public void testMultXandY() {
    float x = 10, y = 20;
    float xOut = m.multX(x, y);
    float yOut = m.multY(x, y);
    assertEquals(x, xOut, 0.0001f);
    assertEquals(y, yOut, 0.0001f);
  }

  @Test
  public void testInvertAndDeterminant() {
    m.set(2, 0, 5, 1, 3, 7);
    float det = m.determinant();
    assertEquals(6, det, 0.0001f);

    boolean invertible = m.invert();
    assertTrue("Matrix should be invertible", invertible);

    PMatrix2D identity = new PMatrix2D(2, 0, 5, 1, 3, 7);
    identity.apply(m);

    assertEquals(1, identity.m00, 0.001f);
    assertEquals(0, identity.m01, 0.001f);
    assertEquals(0, identity.m10, 0.001f);
    assertEquals(1, identity.m11, 0.001f);
  }

  @Test
  public void testIdentityWarped() {
    assertTrue(m.isIdentity());
    assertFalse(m.isWarped());

    m.translate(10, 20);
    assertFalse(m.isIdentity());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTranslate3DThrows() {
    m.translate(1, 2, 3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRotateXThrows() {
    m.rotateX(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRotateYThrows() {
    m.rotateY(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testScale3DThrows() {
    m.scale(1, 2, 3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testApplyPMatrix3DThrows() {
    PMatrix3D m3d = new PMatrix3D(1, 0, 0, 0,
                                   0, 1, 0, 0,
                                   0, 0, 1, 0,
                                   0, 0, 0, 1);
    m.apply(m3d);
  }

  @Test
  public void testGetArray() {
    m.set(new float[]{1, 2, 0, 0, 1, 0});
    float[] arr = m.get(null);
    assertEquals(1, arr[0], 0.0001f);
    assertEquals(2, arr[1], 0.0001f);
    assertEquals(0, arr[2], 0.0001f);
  }
}
