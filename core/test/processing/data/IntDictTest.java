package processing.data;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class IntDictTest {

    private IntDict intDict;

    @Before
    public void setUp() {
        intDict = new IntDict();
    }

    @Test
    public void testDefaultConstructor() {
        assertEquals(0, intDict.size());
    }

    @Test
    public void testAddAndGet() {
        intDict.set("key1", 5);
        intDict.set("key2", 10);
        assertEquals(5, intDict.get("key1"));
        assertEquals(10, intDict.get("key2"));
    }

    @Test
    public void testHasKey() {
        intDict.set("key1", 5);
        assertTrue(intDict.hasKey("key1"));
        assertFalse(intDict.hasKey("key3"));
    }

    @Test
    public void testRemove() {
        intDict.set("key1", 5);
        intDict.set("key2", 10);

        assertEquals(5, intDict.remove("key1"));
        assertFalse(intDict.hasKey("key1"));
        assertEquals(10, intDict.get("key2"));
    }

    @Test
    public void testMinAndMax() {
        intDict.set("key1", 5);
        intDict.set("key2", 15);
        intDict.set("key3", 10);

        assertEquals("key1", intDict.minKey());
        assertEquals("key2", intDict.maxKey());
        assertEquals(5, intDict.minValue());
        assertEquals(15, intDict.maxValue());
    }

    @Test
    public void testResize() {
        intDict.set("key1", 5);
        intDict.set("key2", 10);
        intDict.set("key3", 15);

        intDict.resize(2);
        assertEquals(2, intDict.size());
        assertTrue(intDict.hasKey("key1"));
        assertTrue(intDict.hasKey("key2"));
        assertFalse(intDict.hasKey("key3"));
    }

    @Test
    public void testIncrementFunctionality() {
        intDict.set("key1", 1);
        intDict.increment("key1");
        assertEquals(2, intDict.get("key1"));

        intDict.increment(intDict); // Merging the intDict into itself
        assertEquals(4, intDict.get("key1"));
    }

    @Test
    public void testSortValues() {
        intDict.set("key1", 40);
        intDict.set("key2", 10);
        intDict.set("key3", 20);

        intDict.sortValues();

        assertEquals("key2", intDict.key(0));
        assertEquals("key3", intDict.key(1));
        assertEquals("key1", intDict.key(2));
    }

    @Test
    public void testSortKeys() {
        intDict.set("bKey", 40);
        intDict.set("aKey", 10);
        intDict.set("cKey", 20);

        intDict.sortKeys();

        assertEquals("aKey", intDict.key(0));
        assertEquals("bKey", intDict.key(1));
        assertEquals("cKey", intDict.key(2));
    }

    @Test
    public void testBufferedReaderConstructor() {
        String data = "key1\t100\nkey2\t200";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        IntDict intDictFromReader = new IntDict(reader);

        assertEquals(2, intDictFromReader.size());
        assertEquals(100, intDictFromReader.get("key1"));
        assertEquals(200, intDictFromReader.get("key2"));
    }

    @Test
    public void testCloneFunctionality() {
        intDict.set("key1", 1);
        intDict.set("key2", 2);

        IntDict cloned = intDict.copy();

        assertEquals(2, cloned.size());
        assertEquals(1, cloned.get("key1"));
        assertEquals(2, cloned.get("key2"));
    }

    @Test
    public void testDivision() {
        intDict.set("key1", 10);
        intDict.div("key1", 2);
        assertEquals(5, intDict.get("key1"));
    }

    @Test
    public void testToJSON() {
        intDict.set("key1", 5);
        intDict.set("key2", 10);

        String json = intDict.toJSON();
        assertTrue(json.contains("\"key1\": 5"));
        assertTrue(json.contains("\"key2\": 10"));
    }
}
