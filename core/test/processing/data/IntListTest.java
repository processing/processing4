package processing.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * IntList.java has two fields:
 * 1. count - the number of elements currently stored in the list with an initial value of 0.
 * 2. int[] data - An array to store elements with an initial capacity of 10.
 */
public class IntListTest {
    @Test
    public void testDefaultConstructor() {
        IntList testedList = new IntList();
        assertEquals(0, testedList.size());
        assertEquals(10, testedList.data.length);
    }

    @Test
    public void testConstructorWithLength() {
        IntList testedList = new IntList(20);
        assertEquals(0, testedList.size());
        assertEquals(20, testedList.data.length);
    }

    @Test
    public void testConstructorWithArray() {
        int[] source = {1, 2};
        IntList testedList = new IntList(source);
        assertEquals(2, testedList.size());
        assertEquals(2, testedList.data.length);

        assertEquals(1, testedList.get(0));
        assertEquals(2, testedList.get(1));
    }

    @Test
    public void testConstructorWithIterableObject() {
        List<Object> source = new ArrayList<>(Arrays.asList(1, "2", null, 4.5, -1));
        IntList testedList = new IntList(source);
        assertEquals(5, testedList.size());

        int[] expected = {1, 2, 0, 4, -1};
        assertArrayEquals(expected, testedList.values());
    }
    @Test
    public void testConstructorWithObject() {
        String eleStr = "Hello";
        int eleInt = 10;
        float eleFloat = 1.2f;
        Object eleObj = new Object();

        IntList testedList = new IntList(eleStr, eleInt, eleFloat, eleObj);

        int[] expected = {0, 10, 1, 0};
        assertArrayEquals(expected, testedList.values());
    }
    @Test
    public void testFromRangeWithStopIndex() {
        IntList originalList = new IntList(new int[]{5,10,15,20,25});
        IntList result = originalList.fromRange(2);
        assertArrayEquals(new int[]{0,1}, result.values());
    }
    @Test
    public void testFromRangeWithStartAndStopIndex() {
        IntList originalList = new IntList(new int[]{5,10,15,20,25});
        IntList result = originalList.fromRange(1,3);
        assertArrayEquals(new int[]{1,2}, result.values());
    }
    @Test
    public void testClear() {
        IntList testedList = new IntList(new int[]{1, 2, 3});
        testedList.clear();
        assertEquals(0, testedList.size());
    }
    @Test
    public void testResize() {
        IntList testedList = new IntList(new int[]{1, 2, 3});
        testedList.resize(5);
        assertEquals(5, testedList.size());
        assertEquals(5, testedList.data.length);
    }
    @Test
    public void testSet() {
        IntList testedList = new IntList();
        testedList.set(0, 20);
        assertEquals(1, testedList.size());
        assertEquals(20, testedList.get(0));

        testedList.set(100, 2000);
        assertEquals(101, testedList.size());
        assertEquals(2000, testedList.get(100));
    }
    @Test
    public void testPush() {
        IntList testedList = new IntList();
        testedList.push(100);
        assertEquals(1, testedList.size());
        assertEquals(100, testedList.get(0));
    }
    @Test
    public void testAppendWithInt() {
        IntList testedList = new IntList();
        testedList.append(100);
        assertEquals(1, testedList.size());
        assertEquals(100, testedList.get(0));
    }
    @Test
    public void testAppendWithIntArray() {
        IntList testedList = new IntList();
        int[] source = {10, 20, 30};
        testedList.append(source);
        assertArrayEquals(source, testedList.values());
    }
    @Test
    public void testAppendWithIntList() {
        IntList testedList = new IntList();
        IntList source = new IntList(new int[]{10, 20, 30});
        testedList.append(source);
        assertArrayEquals(source.values(), testedList.values());
    }
    @Test
    public void testAppendUnique() {
        IntList testedList = new IntList(new int[]{10, 20, 30});
        testedList.appendUnique(100);
        assertArrayEquals(new int[]{10, 20, 30, 100}, testedList.values());
    }

    @Test
    public void testPop() {
        IntList testedList = new IntList(new int[]{10, 20});

        assertEquals(20,testedList.pop());
        assertEquals(1,testedList.size());
        assertEquals(10,testedList.pop());
        assertEquals(0,testedList.size());
    }

    @Test
    public void testPopOnEmptyIntListThrowsException() {
        IntList testedList = new IntList();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            testedList.pop();
        });

        assertEquals("Can't call pop() on an empty list", exception.getMessage());
    }

    @Test
    public void testRemoveWithIndexGreaterThanSize() {
        IntList testedList = new IntList();
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> testedList.remove(3));
    }

    @Test
    public void testRemoveWithIndexLessThanZeroThrowsException() {
        IntList testedList = new IntList();
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> testedList.remove(-1));
    }

    @Test
    public void testRemoveWithIndex() {
        IntList testedList = new IntList(new int[]{1, 2, 3});
        assertEquals(2,testedList.remove(1));
        assertEquals(2,testedList.size());
        assertArrayEquals(new int[]{1, 3},testedList.values());
    }
    @Test
    public void testRemoveValue() {
        IntList testedList = new IntList(new int[]{10, 20, 20});
        assertEquals(1,testedList.removeValue(20));
        assertEquals(-1,testedList.removeValue(100));
    }
    @Test
    public void testRemoveValues() {
        IntList testedList = new IntList(new int[]{10, 20, 20});
        assertEquals(2,testedList.removeValues(20));
    }

    @Test
    public void testInsertWithInvalidIndexThrowsException() {
        IntList testedList = new IntList();

        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> testedList.insert(1, 20));

        IllegalArgumentException negativeIndex = assertThrows(IllegalArgumentException.class,
                () -> testedList.insert(-1, 20));
        assertEquals("insert() index cannot be negative: it was -1", negativeIndex.getMessage());
    }

    @Test
    public void testInsertWithValidIndex() {
        IntList testedList = new IntList();
        testedList.insert(0, 10);
        assertEquals(10, testedList.get(0));
    }

    @Test
    public void testInsertWithArray() {
        IntList testedList = new IntList(new int[]{5, 5, 5});
        int[] source = {100, 200};
        testedList.insert(1, source);
        int[] expectedList = new int[]{5, 100, 200, 5, 5};
        assertArrayEquals(expectedList, testedList.values());
    }

    @Test
    public void testInsertWithIntList() {
        IntList testedList = new IntList(new int[]{5, 5, 5});
        IntList source = new IntList(new int[]{100, 200});
        testedList.insert(1, source);
        int[] expectedList = new int[]{5, 100, 200, 5, 5};
        assertArrayEquals(expectedList, testedList.values());
    }

    @Test
    public void testIndex() {
        IntList testedList = new IntList(new int[]{5, 5, 5});
        assertEquals(0,testedList.index(5));
        assertEquals(-1,testedList.index(10));
    }

    @Test
    public void testHasValue() {
        IntList testedList = new IntList(new int[]{1, 2, 3});
        assertTrue(testedList.hasValue(3));
        assertFalse(testedList.hasValue(100));
    }

    @Test
    public void testIncrementWithIndex() {
        IntList testedList = new IntList(new int[] {20});
        testedList.increment(0);
        assertEquals(21,testedList.get(0));
    }
    @Test
    public void testIncrementWithIndexGreaterThanSize() {
        IntList testedList = new IntList(new int[] {20});
        testedList.increment(5);
        assertEquals(1,testedList.get(5));
    }
    @Test
    public void testAddWithValidIndex() {
        IntList testedList = new IntList(new int[] {20});
        testedList.add(0, 20);
        assertEquals(40,testedList.get(0));
    }

    @Test
    public void testAddWithInvalidIndexThrowsException() {
        IntList testedList = new IntList(new int[] {20});
        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                ()->testedList.add(5, 20));

        assertEquals("The list size is 1. You cannot add() to element 5.",exception.getMessage());
    }
    @Test
    public void testSubWithValidIndex() {
        IntList testedList = new IntList(new int[] {20});
        testedList.sub(0, 20);
        assertEquals(0,testedList.get(0));
    }

    @Test
    public void testSubWithInvalidIndexThrowsException() {
        IntList testedList = new IntList(new int[] {20});
        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                ()->testedList.sub(5, 20));

        assertEquals("The list size is 1. You cannot sub() to element 5.",exception.getMessage());
    }
    @Test
    public void testMultWithValidIndex() {
        IntList testedList = new IntList(new int[] {20});
        testedList.mult(0, 20);
        assertEquals(400,testedList.get(0));
    }

    @Test
    public void testMultWithInvalidIndexThrowsException() {
        IntList testedList = new IntList(new int[] {20});
        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                ()->testedList.mult(5, 20));

        assertEquals("The list size is 1. You cannot mult() to element 5.",exception.getMessage());
    }
    @Test
    public void testDivWithValidIndex() {
        IntList testedList = new IntList(new int[] {20});
        testedList.div(0, 20);
        assertEquals(1,testedList.get(0));
    }

    @Test
    public void testDivWithInvalidIndexThrowsException() {
        IntList testedList = new IntList(new int[] {20});
        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                ()->testedList.div(5, 20));

        assertEquals("The list size is 1. You cannot div() to element 5.",exception.getMessage());
    }
    @Test
    public void testMinWithValidIndex() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        assertEquals(-5, testedList.min());
    }

    @Test
    public void testMinWithInvalidIndexThrowsException() {
        IntList testedList = new IntList();
        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->testedList.min());

        assertEquals("Cannot use min() on an empty IntList.",exception.getMessage());
    }
    @Test
    public void testMinIndexWithValidIndex() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        assertEquals(2, testedList.minIndex());
    }

    @Test
    public void testMinIndexWithInvalidIndexThrowsException() {
        IntList testedList = new IntList();
        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->testedList.minIndex());

        assertEquals("Cannot use minIndex() on an empty IntList.",exception.getMessage());
    }

    @Test
    public void testMaxWithValidIndex() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        assertEquals(20, testedList.max());
    }

    @Test
    public void testMaxWithInvalidIndexThrowsException() {
        IntList testedList = new IntList();
        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->testedList.max());

        assertEquals("Cannot use max() on an empty IntList.",exception.getMessage());
    }

    @Test
    public void testMaxIndexWithValidIndex() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        assertEquals(0, testedList.maxIndex());
    }

    @Test
    public void testMaxIndexWithInvalidIndexThrowsException() {
        IntList testedList = new IntList();
        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->testedList.maxIndex());

        assertEquals("Cannot use maxIndex() on an empty IntList.",exception.getMessage());
    }
    @Test
    public void testSumLong() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        assertEquals(25, testedList.sumLong());
    }
    @Test
    public void testSumWithValidIntegerValue() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        assertEquals(25, testedList.sum());
    }
    @Test
    public void testSumGreaterThanMaxIntegerValueThrowsException() {
        int value = Integer.MAX_VALUE;
        IntList testedList = new IntList(new int[] {value, 1});
        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->testedList.sum());

        assertEquals("sum() exceeds 2147483647, use sumLong()",exception.getMessage());
    }
    @Test
    public void testSumLessThanMinIntegerValueThrowsException() {
        int value = Integer.MIN_VALUE;
        IntList testedList = new IntList(new int[] {value, -1});
        RuntimeException exception = assertThrows(RuntimeException.class,
                ()->testedList.sum());

        assertEquals("sum() less than -2147483648, use sumLong()",exception.getMessage());
    }
    @Test
    public void testSort() {
        IntList testedList = new IntList(new int[] {20, 10, -5});
        testedList.sort();
        assertArrayEquals(new int[]{-5,10,20}, testedList.values());
    }
    @Test
    public void testSortReverse() {
        IntList testedList = new IntList(new int[] {20, 10, 100});
        testedList.sortReverse();
        assertArrayEquals(new int[]{100,20,10}, testedList.values());
    }
    @Test
    public void testReverse() {
        IntList testedList = new IntList(new int[] {20, 10, 100});
        testedList.reverse();
        assertArrayEquals(new int[]{100,10,20}, testedList.values());
    }
    @Test
    public void testChoice() {
        IntList testedList = new IntList(new int[] {20, 10, 100});
        int num = testedList.choice();
        assertTrue(testedList.hasValue(num));
    }
    @Test
    public void testChoiceOnEmptyIntListThrowsException() {
        IntList testedList = new IntList();
        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                ()->testedList.choice());
        assertEquals("No entries in this IntList", exception.getMessage());
    }
    @Test
    public void testRemoveChoice() {
        IntList testedList = new IntList(new int[] {20, 10, 100});
        int num = testedList.removeChoice();
        assertEquals(2, testedList.size());
    }

    @Test
    public void testRemoveChoiceOnEmptyIntListThrowsException() {
        IntList testedList = new IntList();
        ArrayIndexOutOfBoundsException exception = assertThrows(ArrayIndexOutOfBoundsException.class,
                ()->testedList.removeChoice());
        assertEquals("No entries in this IntList", exception.getMessage());
    }
    @Test
    public void testCopy() {
        IntList originalList = new IntList(new int[] {20, 10, 100});
        IntList copyList = originalList.copy();
        assertArrayEquals(originalList.values(), copyList.values());
        assertEquals(originalList.size(), copyList.size());
    }
    @Test
    public void testToArray() {
        IntList originalList = new IntList(new int[] {20, 10, 100});
        int[] result = originalList.toArray();
        assertArrayEquals(new int[] {20, 10, 100}, result);
    }
    @Test
    public void testToArrayWithDestinationArray() {
        IntList originalList = new IntList(new int[] {20, 10, 100});
        int[] oversize = new int[100];
        int[] result = originalList.toArray(oversize);
        assertArrayEquals(new int[] {20, 10, 100}, result);
        assertEquals(3, result.length);
    }
    @Test
    public void testToArrayWithDestinationArrayNull() {
        IntList originalList = new IntList(new int[] {20, 10, 100});
        int[] initialIsNull = null;
        int[] result = originalList.toArray(initialIsNull);
        assertArrayEquals(new int[] {20, 10, 100}, result);
        assertEquals(3, result.length);
    }
    @Test
    public void testGetPercent() {
        IntList originalList = new IntList(new int[] {5, 10, 5});
        FloatList result = originalList.getPercent();
        assertArrayEquals(new float[] {0.25f, 0.5f, 0.25f}, result.values(),  1e-6f);
    }
    @Test
    public void testGetSubset() {
        IntList originalList = new IntList(new int[] {5, 10, 20});
        IntList result = originalList.getSubset(1);
        assertArrayEquals(new int[]{10, 20}, result.values());
    }
    @Test
    public void testJoin() {
        IntList originalList = new IntList(new int[] {5, 10});
        assertEquals("5&10", originalList.join("&"));
    }
    @Test
    public void testJoinWithEmptyList() {
        IntList originalList = new IntList();
        assertEquals("", originalList.join("&"));
    }
}
