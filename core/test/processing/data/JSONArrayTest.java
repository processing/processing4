package processing.data;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class JSONArrayTest {
    private JSONArray jsonArray;

    @Before
    public void setUp() {
        jsonArray = new JSONArray();
    }

    // Verify getString method works as intended
    @Test
    public void testGetString() {
        JSONArray array = JSONArray.parse("['sample', 'text']");
        assertEquals("sample", array.getString(0));
        assertEquals("text", array.getString(1));
    }

    // Verify getString method with default return value works as intended
    @Test
    public void testGetStringWithDefault() {
        JSONArray array = JSONArray.parse("['sample', null]");
        assertEquals("sample", array.getString(0, "default"));
        assertEquals("random", array.getString(1, "random"));
        assertEquals("default", array.getString(2, "default"));
    }

    // Verify getString method with invalid type throws exception
    @Test(expected = RuntimeException.class)
    public void testGetStringWithInvalidType() {
        JSONArray array = JSONArray.parse("[132]");
        array.getString(0);
    }

    // Verify list creation from String list works as intended
    @Test
    public void testJSONArrayFromStringList() {
        StringList stringList = new StringList();
        stringList.append("some");
        stringList.append("text");

        JSONArray array = new JSONArray(stringList);

        assertEquals(2, array.size());
        assertEquals("some", array.getString(0));
        assertEquals("text", array.getString(1));
    }

    // Verify getInt method works as intended
    @Test
    public void testGetInt() {
        JSONArray array = JSONArray.parse("[ 118, 999]");
        assertEquals(118, array.getInt(0));
        assertEquals(999, array.getInt(1));
    }

    // Verify getInt method with default return value works as intended
    @Test
    public void testGetIntWithDefault() {
        JSONArray array = JSONArray.parse("[72, null]");
        assertEquals(72, array.getInt(0, -1));
        assertEquals(-1, array.getInt(1, -1));
        assertEquals(-1, array.getInt(2, -1));
    }

    // Verify getInt method with invalid type throws exception
    @Test(expected = RuntimeException.class)
    public void testGetIntWithInvalidType() {
        JSONArray array = JSONArray.parse("['test']");
        array.getInt(0);
    }

    // Verify list creation from int list works as intended
    @Test
    public void testJSONArrayFromIntList() {
        IntList intList = new IntList();
        intList.append(1);
        intList.append(2);
        intList.append(3);

        JSONArray array = new JSONArray(intList);

        assertEquals(3, array.size());
        assertEquals(1, array.getInt(0));
    }

    // Verify getLong method works as intended
    @Test
    public void testGetLong() {
        JSONArray array = JSONArray.parse("[ 9223372036854775807, 72]");
        assertEquals(9223372036854775807L, array.getLong(0));
        assertEquals(72L, array.getLong(1));
    }

    // Verify getLong method with default return value works as intended
    @Test
    public void testGetLongWithDefault() {
        JSONArray array = JSONArray.parse("[9223372036854775807, null]");
        assertEquals(9223372036854775807L, array.getLong(0, -1));
        assertEquals(-1, array.getLong(1, -1));
        assertEquals(-1L, array.getLong(2, -1L));
    }

    // Verify getLong method with invalid type throws exception
    @Test(expected = RuntimeException.class)
    public void testGetLongWithInvalidType() {
        JSONArray array = JSONArray.parse("['test']");
        array.getLong(0);
    }

    // Verify getFloat method works as intended
    @Test
    public void testGetFloat() {
        JSONArray array = JSONArray.parse("[3.1, 21.03, 0.0]");
        assertEquals(3.1f, array.getFloat(0), 0.0001);
        assertEquals(21.03f, array.getFloat(1), 0.0001);
        assertEquals(0.0f, array.getFloat(2), 0.0001);
    }

    // Verify getFloat method with default return value works as intended
    @Test
    public void testGetFloatWithDefault() {
        JSONArray array = JSONArray.parse("[3.14159, null]");
        assertEquals(3.14159f, array.getFloat(0, -1.0f), 0.0001);
        assertEquals(-1.0f, array.getFloat(1, -1.0f), 0.0001);
        assertEquals(-1.0f, array.getFloat(2, -1.0f), 0.0001);
    }

    // Verify getFloat method with invalid type throws exception
    @Test(expected = RuntimeException.class)
    public void testGetFloatWithInvalidType() {
        JSONArray array = JSONArray.parse("['not a float']");
        array.getFloat(0);
    }

    // Verify list creation from float list works as intended
    @Test
    public void testJSONArrayFromFloatList() {
        FloatList floatList = new FloatList();
        floatList.append(1.1f);
        floatList.append(2.2f);

        JSONArray array = new JSONArray(floatList);

        assertEquals(2, array.size());
        assertEquals(2.2f, array.getFloat(1), 0.0001f);
    }

    // Verify getDouble method works as intended
    @Test
    public void testGetDouble() {
        JSONArray array = JSONArray.parse("[3.141592653589793, 2.718281828459045]");
        assertEquals(3.141592653589793, array.getDouble(0), 0.0000000001);
        assertEquals(2.718281828459045, array.getDouble(1), 0.0000000001);
    }

    // Verify getDouble method with default return value works as intended
    @Test
    public void testGetDoubleWithDefault() {
        JSONArray array = JSONArray.parse("[3.141592653589793, null]");
        assertEquals(3.141592653589793, array.getDouble(0, -1.0), 0.0000000001);
        assertEquals(-1.0, array.getDouble(1, -1.0), 0.0000000001);
        assertEquals(-1.0, array.getDouble(2, -1.0), 0.0000000001);
    }

    // Verify getDouble method with invalid type throws exception
    @Test(expected = RuntimeException.class)
    public void testGetDoubleWithInvalidType() {
        JSONArray array = JSONArray.parse("['test']");
        array.getDouble(0);
    }

    // Verify getBoolean method works as intended
    @Test
    public void testGetBoolean() {
        JSONArray array = JSONArray.parse("[true, false, 'true', 'false']");

        assertTrue(array.getBoolean(0));
        assertFalse(array.getBoolean(1));
        assertTrue(array.getBoolean(2));
        assertFalse(array.getBoolean(3));
    }

    // Verify getBoolean method with default return value works as intended
    @Test
    public void testGetBooleanWithDefault() {
        JSONArray array = JSONArray.parse("[true, null]");
        assertTrue(array.getBoolean(0, false));
        assertFalse(array.getBoolean(1, false));
        assertFalse(array.getBoolean(2, false));
    }

    // Verify getDouble method with invalid type throws exception
    @Test(expected = RuntimeException.class)
    public void testGetBooleanWithInvalidType() {
        JSONArray array = JSONArray.parse("[2]");
        array.getBoolean(0);
    }

    // Verify sample array is parsed correctly
    @Test
    public void testParseValidJSONArray() {
        JSONArray array = JSONArray.parse("[1, 2, 3, 'test']");

        assertEquals(4, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
        assertEquals("test", array.get(3));
    }

    // Verify sample array is parsed correctly
    @Test(expected = RuntimeException.class)
    public void testParseInvalidJSONArray() {
        new JSONArray("not an array");
    }

    // Verify sample array with special chars is parsed correctly
    @Test
    public void testParseWithSpecialCharacters() {
        JSONArray array = JSONArray.parse("['quote\\\"quote', 'backslash\\\\backslash']");

        assertEquals("quote\"quote", array.getString(0));
        assertEquals("backslash\\backslash", array.getString(1));
    }

    // Verify sample array from reader is parsed correctly
    @Test
    public void testParseValidJSONArrayFromReader() {
        String jsonStr = "[1, 2, 3, 'test']";
        StringReader reader = new StringReader(jsonStr);

        JSONArray array = new JSONArray(reader);

        assertEquals(4, array.size());
        assertEquals(1, array.get(0));
        assertEquals(2, array.get(1));
        assertEquals(3, array.get(2));
        assertEquals("test", array.get(3));
    }

    // Verify nested arrays are parsed correctly
    @Test
    public void testGetJSONArray() {
        JSONArray array = JSONArray.parse("[[7, 123], ['test', 'array', 'blah']]");

        JSONArray nested_1 = array.getJSONArray(0);
        JSONArray nested_2 = array.getJSONArray(1);

        assertEquals(2, nested_1.size());
        assertEquals(3, nested_2.size());
        assertEquals(7, nested_1.getInt(0));
        assertEquals("blah", nested_2.getString(2));
    }

    // Verify getJSONObject method works as intended
    @Test
    public void testGetJSONObject() {
        JSONArray array = JSONArray.parse("[{'dict key' : 'some value', 'num' : 123}]");

        JSONObject dict_obj = array.getJSONObject(0);

        assertEquals("some value", dict_obj.getString("dict key"));
        assertEquals(123, dict_obj.getInt("num"));
    }

    // Verify the set and remove features work as intended
    @Test
    public void testSetAndRemove() {
        JSONArray array = JSONArray.parse("[1,2,3]");

        array.setInt(1, 42);
        assertEquals(42, array.getInt(1));

        array.remove(1);
        assertEquals(2, array.size());
        assertEquals(3, array.getInt(1));
    }

}