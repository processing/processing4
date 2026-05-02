package processing.core;

import static org.junit.Assert.*;
import org.junit.Test;

public class PMatrix3DTest {

  @Test
  public void testConstructorsResetAndGet() {
    PMatrix3D m = new PMatrix3D();
    float[] vals = m.get(null);
    assertEquals(1, vals[0], 1e-6);
    assertEquals(1, vals[5], 1e-6);
    assertEquals(1, vals[10], 1e-6);
    assertEquals(1, vals[15], 1e-6);
    
    m.m00 = 2;
    m.reset();
    vals = m.get(null);
    assertEquals(1, vals[0], 1e-6);
  }

  @Test
  public void testSetAndGetMethods() {
    PMatrix3D m = new PMatrix3D();
    float[] source = {
      2, 3, 4, 5,
      6, 7, 8, 9,
      10,11,12,13,
      14,15,16,17
    };
    m.set(source);
    float[] target = m.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(source[i], target[i], 1e-6);
    }
    
    m.set(1, 2, 3, 4, 5, 6);
    target = m.get(null);
    assertEquals(1, target[0], 1e-6);
    assertEquals(2, target[1], 1e-6);
    assertEquals(0, target[2], 1e-6);
    assertEquals(3, target[3], 1e-6);
    
    PMatrix2D m2d = new PMatrix2D(1, 2, 3, 4, 5, 6);
    m.set(m2d);
    target = m.get(null);
    assertEquals(1, target[0], 1e-6);
    assertEquals(2, target[1], 1e-6);
    assertEquals(0, target[2], 1e-6);
    assertEquals(3, target[3], 1e-6);
  }

  @Test
  public void testTranslate() {
    PMatrix3D m = new PMatrix3D();
    m.translate(0, 0, 0);
    float[] original = new PMatrix3D().get(null);
    float[] result = m.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(original[i], result[i], 1e-6);
    }
    
    m.reset();
    m.translate(10, -5, 3);
    result = m.get(null);
    assertEquals(10, result[3], 1e-6);
    assertEquals(-5, result[7], 1e-6);
    assertEquals(3, result[11], 1e-6);
    assertEquals(1, result[15], 1e-6);
  }

  @Test
  public void testRotateAndShear() {
    PMatrix3D m = new PMatrix3D();
    m.reset();
    m.rotate(0);
    float[] original = new PMatrix3D().get(null);
    float[] result = m.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(original[i], result[i], 1e-6);
    }
    
    m.reset();
    m.rotateZ((float) Math.PI / 2);
    result = m.get(null);
    assertEquals(0, result[0], 1e-6);
    assertEquals(-1, result[1], 1e-6);
    assertEquals(1, result[4], 1e-6);
    assertEquals(0, result[5], 1e-6);
    
    m.reset();
    m.shearX((float) Math.PI / 4);
    result = m.get(null);
    float expectedT = (float) Math.tan(Math.PI / 4);
    assertEquals(expectedT, result[1], 1e-6);
    
    m.reset();
    m.shearY((float) Math.PI / 4);
    result = m.get(null);
    assertEquals(expectedT, result[4], 1e-6);
  }

  @Test
  public void testScale() {
    PMatrix3D m = new PMatrix3D();
    m.reset();
    m.scale(1);
    float[] original = new PMatrix3D().get(null);
    float[] result = m.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(original[i], result[i], 1e-6);
    }
    
    m.reset();
    m.scale(2, 3, 4);
    result = m.get(null);
    assertEquals(2, result[0], 1e-6);
    assertEquals(3, result[5], 1e-6);
    assertEquals(4, result[10], 1e-6);
    
    m.reset();
    m.scale(0, 1, 1);
    result = m.get(null);
    assertEquals(0, result[0], 1e-6);
  }

  @Test
  public void testApplyAndPreApply() {
    PMatrix3D m = new PMatrix3D();
    m.reset();
    PMatrix3D n = new PMatrix3D(
      2, 3, 4, 5,
      6, 7, 8, 9,
      10,11,12,13,
      14,15,16,17
    );
    m.apply(n);
    float[] result = m.get(null);
    float[] expected = n.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(expected[i], result[i], 1e-6);
    }
    
    m.reset();
    m.preApply(n);
    result = m.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(expected[i], result[i], 1e-6);
    }
  }

  @Test
  public void testMultMethods() {
    PMatrix3D m = new PMatrix3D();
    m.reset();
    PVector p = new PVector(1, 2, 3);
    PVector resultP = m.mult(p, null);
    assertEquals(1, resultP.x, 1e-6);
    assertEquals(2, resultP.y, 1e-6);
    assertEquals(3, resultP.z, 1e-6);
    
    float[] vec3 = {1, 2, 3};
    float[] out3 = m.mult(vec3, new float[3]);
    assertEquals(1, out3[0], 1e-6);
    assertEquals(2, out3[1], 1e-6);
    assertEquals(3, out3[2], 1e-6);
    
    float[] vec4 = {1, 2, 3, 4};
    float[] out4 = m.mult(vec4, new float[4]);
    for (int i = 0; i < 4; i++) {
      assertEquals(vec4[i], out4[i], 1e-6);
    }
    
    try {
      m.mult(vec3, vec3);
      fail("Expected RuntimeException for identical source and target arrays.");
    } catch (RuntimeException e) {
      // Exception expected.
    }
    
    m.reset();
    m.translate(10, 20, 30);
    float x = m.multX(1, 2, 3);
    float y = m.multY(1, 2, 3);
    float z = m.multZ(1, 2, 3);
    float w = m.multW(1, 2, 3);
    assertEquals(1 + 10, x, 1e-6);
    assertEquals(2 + 20, y, 1e-6);
    assertEquals(3 + 30, z, 1e-6);
    assertEquals(1, w, 1e-6);
  }

  @Test
  public void testTransposeAndDeterminant() {
    PMatrix3D m = new PMatrix3D(
      2, 3, 4, 5,
      6, 7, 8, 9,
      10,11,12,13,
      14,15,16,17
    );
    float det = m.determinant();
    m.transpose();
    float det2 = m.determinant();
    assertEquals(det, det2, 1e-6);
  }

  @Test
  public void testInvert() {
    PMatrix3D m = new PMatrix3D();
    m.reset();
    boolean inverted = m.invert();
    assertTrue(inverted);
    float[] result = m.get(null);
    PMatrix3D identity = new PMatrix3D();
    float[] idArr = identity.get(null);
    for (int i = 0; i < 16; i++) {
      assertEquals(idArr[i], result[i], 1e-6);
    }
    
    m.reset();
    m.scale(0, 1, 1);
    inverted = m.invert();
    assertFalse(inverted);
  }
}
