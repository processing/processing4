package processing.data;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Double.NaN;
import static org.junit.Assert.*;

public class DoubleListTest{
    @Test
    public void testDefaultConstructor() {
        // 10 is the default value in DoubleList
        DoubleList testedList = new DoubleList();
        assertEquals(0, testedList.size());
        assertEquals(10, testedList.data.length);
    }

    @Test
    public void testConstructorWithLength() {
        DoubleList testedList = new DoubleList(20);
        assertEquals(0, testedList.size());
        assertEquals(20, testedList.data.length);
    }

    @Test
    public void testConstructorWithArray() {
        double[] source = {1.0, 2.0};
        DoubleList testedList = new DoubleList(source);
        assertEquals(2, testedList.size());
        assertEquals(2, testedList.data.length);

        assertEquals(1, testedList.get(0),0);
        assertEquals(2, testedList.get(1),0);
    }

    @Test
    public void testConstructorWithEmptyArray() {
        double[] source = {};
        DoubleList testedList = new DoubleList(source);
        assertEquals(0, testedList.size());
        assertEquals(0, testedList.data.length);
    }

    @Test
    public void testConstructorWithIterableObject() {
        List<Object> source = new ArrayList<>(Arrays.asList("1.1", "test", null, 4.5, -1));
        DoubleList testedList = new DoubleList(source);
        assertEquals(5, testedList.size());

        double[] expected = {1.1, NaN, NaN, 4.5, -1};

        assertEquals(expected[0], testedList.get(0), 1e-7);
        assertEquals(expected[1], testedList.get(1), 0);
        assertTrue(Double.isNaN(testedList.get(1)));
        assertEquals(expected[2], testedList.get(2), 0);
        assertTrue(Double.isNaN(testedList.get(2)));
        assertEquals(expected[3], testedList.get(3), 0);
        assertEquals(expected[4], testedList.get(4), 0);

        assertArrayEquals(expected, testedList.values(), 1e-7);
    }

    @Test
    public void testConstructorWithObject() {
        String eleStr = "Hello";
        double eleDouble = 10.0;
        float eleFloat = 1.2f;
        Object eleObj = new Object();

        DoubleList testedList = new DoubleList(eleStr, eleDouble, eleFloat, eleObj);

        double[] expected = {NaN, 10.0, 1.2, NaN};

        assertEquals(expected[0], testedList.get(0), 0);
        assertTrue(Double.isNaN(testedList.get(0)));
        assertEquals(expected[1], testedList.get(1), 0);
        assertEquals(expected[2], testedList.get(2), 1e-7);
        assertEquals(expected[3], testedList.get(3), 0);
        assertTrue(Double.isNaN(testedList.get(3)));

        assertArrayEquals(expected, testedList.values(),1e-7);
    }
}