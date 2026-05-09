package processing.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link FloatList}, mirroring coverage patterns used in {@link IntListTest}.
 */
public class FloatListTest {

  @Test
  public void testDefaultConstructor() {
    FloatList list = new FloatList();
    assertEquals(0, list.size());
    assertEquals(10, list.data.length);
  }

  @Test
  public void testConstructorWithLength() {
    FloatList list = new FloatList(25);
    assertEquals(0, list.size());
    assertEquals(25, list.data.length);
  }

  @Test
  public void testConstructorWithArray() {
    float[] source = {1.5f, 2.5f, -3f};
    FloatList list = new FloatList(source);
    assertEquals(3, list.size());
    assertEquals(1.5f, list.get(0), 0.0001f);
    assertEquals(2.5f, list.get(1), 0.0001f);
    assertEquals(-3f, list.get(2), 0.0001f);
  }

  @Test
  public void testConstructorWithIterableParsesNumbersAndNullAsNaN() {
    List<Object> source = new ArrayList<>(Arrays.asList(1, 2.5f, null, "3.25"));
    FloatList list = new FloatList(source);
    assertEquals(4, list.size());
    assertEquals(1f, list.get(0), 0.0001f);
    assertEquals(2.5f, list.get(1), 0.0001f);
    assertTrue(Float.isNaN(list.get(2)));
    assertEquals(3.25f, list.get(3), 0.0001f);
  }

  @Test
  public void testAppendAndGet() {
    FloatList list = new FloatList();
    list.append(10f);
    list.append(20f);
    assertEquals(2, list.size());
    assertEquals(10f, list.get(0), 0.0001f);
    assertEquals(20f, list.get(1), 0.0001f);
  }

  @Test
  public void testClear() {
    FloatList list = new FloatList(new float[] {1f, 2f});
    list.clear();
    assertEquals(0, list.size());
  }

  @Test
  public void testPopReturnsLastAndRemoves() {
    FloatList list = new FloatList(new float[] {1f, 2f, 3f});
    assertEquals(3f, list.pop(), 0.0001f);
    assertEquals(2, list.size());
    assertEquals(2f, list.get(1), 0.0001f);
  }

  @Test(expected = RuntimeException.class)
  public void testPopOnEmptyThrows() {
    new FloatList().pop();
  }

  @Test
  public void testValuesReturnsCopyOfUsedRange() {
    FloatList list = new FloatList(new float[] {1f, 2f, 3f});
    float[] v = list.values();
    assertEquals(3, v.length);
    assertArrayEquals(new float[] {1f, 2f, 3f}, v, 0.0001f);
    v[0] = 99f;
    assertEquals(1f, list.get(0), 0.0001f);
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testGetOutOfBoundsThrows() {
    new FloatList(new float[] {1f}).get(1);
  }
}
