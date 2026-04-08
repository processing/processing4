package processing.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FloatListTest {
    @Test
    public void testConstructorDefault(){
        FloatList testList = new FloatList();

        assertEquals(0, testList.size());
        assertEquals(10, testList.data.length);
    }

    @Test
    public void testConstructorLength(){
        FloatList testList = new FloatList(15);

        assertEquals(0, testList.size());
        assertEquals(15, testList.data.length);
    }

    @Test
    public void testConstructorArray(){
        FloatList testList = new FloatList(new float[] {1.1F, 2.2F, 3.3F});

        assertEquals(3, testList.size());
        assertEquals(3, testList.data.length);

        assertEquals(1.1F, testList.get(0), 0.0F);
        assertEquals(3.3F, testList.get(2), 0.0F);
    }

    @Test
    public void testConstructorIterableObject(){
        List<Object> src = new ArrayList<>(Arrays.asList("String", 9, 10.4F, 3.7, null));
        FloatList testList = new FloatList(src);
        assertEquals(5, testList.size());

        float[] expected = {Float.NaN, 9.0F, 10.4F, 3.7F, Float.NaN};
        assertEquals(expected[0], testList.get(0), 0.0F);
        assertEquals(expected[1], testList.get(1), 0.0F);
        assertEquals(expected[2], testList.get(2), 0.0F);
        assertEquals(expected[3], testList.get(3), 0.0F);
    }

    @Test
    public void testConstructorObject(){
        String typeStr = "String";
        int typeInt = 21;
        float typeFlt = 4.5F;
        Object typeObj = new Object();

        FloatList testList = new FloatList(typeStr, typeInt, typeFlt, typeObj);

        float[] expected = {Float.NaN, 21.0F, 4.5F, Float.NaN};
        assertEquals(expected[0], testList.get(0), 0.0F);
        assertEquals(expected[1], testList.get(1), 0.0F);
        assertEquals(expected[2], testList.get(2), 0.0F);
        assertEquals(expected[3], testList.get(3), 0.0F);
    }

    @Test
    public void testSize(){
        FloatList testList = new FloatList(new float[]{1.1F, 2.2F, 3.3F});

        assertEquals(3, testList.size());
    }

    @Test
    public void testResize(){
        FloatList testList = new FloatList(new float[]{3.3F, 4.4F, 5.5F});

        assertEquals(3, testList.size());

        testList.resize(10);
        assertEquals(10, testList.size());
        assertEquals(10, testList.data.length);
    }

    @Test
    public void testClear(){
        FloatList testList = new FloatList(new float[]{45.8F, 5.6F, 9.8F});

        assertEquals(3, testList.size());
        testList.clear();
        assertEquals(0, testList.size());
    }
    
    @Test
    public void testGet(){
        FloatList testList = new FloatList(new float[]{4.5F, 7.8F});

        assertEquals(4.5F, testList.get(0), 0.0F);
        assertEquals(7.8F, testList.get(1), 0.0F);
    }

    @Test
    public void testSet(){
        FloatList testList = new FloatList();

        testList.set(0, 18.0F);
        assertEquals(1, testList.size());
        assertEquals(18.0F, testList.get(0), 0.0F);

        testList.set(500, 4.9F);
        assertEquals(501, testList.size());
        assertEquals(4.9F, testList.get(500), 0.0F);
    }

    @Test
    public void testPush(){
        FloatList testList = new FloatList();
        testList.push(34.0F);

        assertEquals(1, testList.size());
        assertEquals(34.0F, testList.get(0), 0.0F);
    }

    @Test
    public void testPop(){
        FloatList testList = new FloatList(new float[]{6.0F, 7.0F});

        assertEquals(7.0F, testList.pop(), 0.0F);
        assertEquals(1, testList.size());

        assertEquals(6.0F, testList.pop(), 0.0F);
        assertEquals(0, testList.size());
    }

    @Test
    public void testRemove(){
        FloatList testList = new FloatList(new float[]{1.0F, 2.0F, 3.0F });

        float removed = testList.remove(1);
        assertEquals(2.0F, removed, 0.0F);
        assertEquals(2, testList.size());
        assertEquals(1.0F, testList.get(0), 0.0F);
        assertEquals(3.0F, testList.get(1), 0.0F);
    }

    @Test
    public void testRemoveValues(){
        FloatList testList = new FloatList(new float[]{1.0F, 2.0F, 2.0F, 3.0F, 2.0F});

        int removed = testList.removeValues(2.0F);
        assertEquals(3, removed);
        assertEquals(2, testList.size());
        assertEquals(1.0F, testList.get(0), 0.0F);
        assertEquals(3.0F, testList.get(1), 0.0F);
    }
    @Test
    public void testReplaceValue() {
        FloatList testList = new FloatList(new float[]{1.0F, 2.0F, 2.0F, 3.0F});

        boolean changed = testList.replaceValue(2.0F, 99.0F);
        assertTrue(changed);
        assertEquals(99.0F, testList.get(1), 0.0F);
        assertEquals(2.0F, testList.get(2), 0.0F);
    }
    @Test
    public void testReplaceValueNotFound() {
        FloatList testList = new FloatList(new float[]{1.0F, 2.0F});

        boolean changed = testList.replaceValue(5.0F, 99.0F);
        assertFalse(changed);
    }
    @Test
    public void testAdd() {
        FloatList testList = new FloatList(new float[]{10.0F, 20.0F});

        testList.add(0, 5.0F);
        assertEquals(15.0F, testList.get(0), 0.0F);
        assertEquals(20.0F, testList.get(1), 0.0F);
    }
}
