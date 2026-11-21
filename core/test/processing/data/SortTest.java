package processing.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Sort.java is an abstract class implementing quicksort with three abstract methods:
 * 1. size() - returns the number of elements
 * 2. compare(int a, int b) - compares elements at two indices
 * 3. swap(int a, int b) - swaps elements at two indices
 */
public class SortTest {

    /**
     * Concrete implementation of Sort for testing using an int array.
     */
    private static class IntArraySort extends Sort {
        int[] data;

        IntArraySort(int[] data) {
            this.data = data;
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public int compare(int a, int b) {
            return Integer.compare(data[a], data[b]);
        }

        @Override
        public void swap(int a, int b) {
            int temp = data[a];
            data[a] = data[b];
            data[b] = temp;
        }
    }

    @Test
    public void testSortAlreadySorted() {
        int[] data = {1, 2, 3, 4, 5};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, data);
    }

    @Test
    public void testSortReversed() {
        int[] data = {5, 4, 3, 2, 1};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, data);
    }

    @Test
    public void testSortUnsorted() {
        int[] data = {3, 1, 4, 1, 5, 9, 2, 6};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{1, 1, 2, 3, 4, 5, 6, 9}, data);
    }

    @Test
    public void testSortSingleElement() {
        int[] data = {42};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{42}, data);
    }

    @Test
    public void testSortEmptyArray() {
        int[] data = {};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{}, data);
    }

    @Test
    public void testSortTwoElements() {
        int[] data = {2, 1};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{1, 2}, data);
    }

    @Test
    public void testSortTwoElementsAlreadySorted() {
        int[] data = {1, 2};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{1, 2}, data);
    }

    @Test
    public void testSortWithDuplicates() {
        int[] data = {3, 3, 3, 3};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{3, 3, 3, 3}, data);
    }

    @Test
    public void testSortWithNegativeNumbers() {
        int[] data = {0, -3, 5, -1, 2};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{-3, -1, 0, 2, 5}, data);
    }

    @Test
    public void testSortWithMixedDuplicatesAndNegatives() {
        int[] data = {4, -2, 4, 0, -2};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{-2, -2, 0, 4, 4}, data);
    }

    @Test
    public void testSizeReflectsArrayLength() {
        int[] data = {10, 20, 30};
        IntArraySort sorter = new IntArraySort(data);
        assertEquals(3, sorter.size());
    }

    @Test
    public void testSwapExchangesElements() {
        int[] data = {10, 20, 30};
        IntArraySort sorter = new IntArraySort(data);
        sorter.swap(0, 2);
        assertArrayEquals(new int[]{30, 20, 10}, data);
    }

    @Test
    public void testCompareReturnsNegativeWhenLess() {
        int[] data = {1, 5};
        IntArraySort sorter = new IntArraySort(data);
        assertTrue(sorter.compare(0, 1) < 0);
    }

    @Test
    public void testCompareReturnsPositiveWhenGreater() {
        int[] data = {5, 1};
        IntArraySort sorter = new IntArraySort(data);
        assertTrue(sorter.compare(0, 1) > 0);
    }

    @Test
    public void testCompareReturnsZeroWhenEqual() {
        int[] data = {3, 3};
        IntArraySort sorter = new IntArraySort(data);
        assertEquals(0, sorter.compare(0, 1));
    }

    @Test
    public void testSortDescendingLargeArray() {
        int[] data = {6, 5, 4, 3, 2, 1};
        IntArraySort sorter = new IntArraySort(data);
        sorter.run();
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, data);
    }
}