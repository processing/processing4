package processing.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSONObjectTest {

    private JSONObject obj;

    @Before
    public void setUp() {
        obj = new JSONObject();
    }

    // Verify set and get long methods work as intended
    @Test
    public void testSetAndGetLong() {
        long value = 9223372036854775807L;
        long defaultValue = 1234567890123456789L;

        obj.setLong("long_key", value);

        assertEquals(value, obj.getLong("long_key"));
        assertEquals(defaultValue, obj.getLong("long_key_not_present", defaultValue));
    }

    // Verify set and get float methods work as intended
    @Test
    public void testSetAndGetFloat() {
        float value = 3.14159f;
        float defaultValue = 1234567890.123456789f;

        obj.setFloat("float_key", value);

        assertEquals(value, obj.getFloat("float_key"), 0.0001f);
        assertEquals(defaultValue, obj.getFloat("float_key_not_present", defaultValue), 0.0001f);
    }

    // Verify set and get double methods work as intended
    @Test
    public void testSetAndGetDouble() {
        double value = 3.14159265359;
        double defaultValue = 1234567890.123456789;

        obj.setDouble("double_key", value);

        assertEquals(value, obj.getDouble("double_key"), 0.0000001);
        assertEquals(defaultValue, obj.getDouble("double_key_not-present", defaultValue), 0.0000001);
    }

    // Verify set and get int methods work as intended
    @Test
    public void testSetAndGetInt() {
        int value = 12;
        int defaultValue = 42;

        obj.setInt("int_key", value);

        assertEquals(value, obj.getInt("int_key"));
        assertEquals(defaultValue, obj.getInt("int_key_not-present", defaultValue));
    }

    // Verify set and get bool methods work as intended
    @Test
    public void testSetAndGetBool() {
        boolean value = true;
        boolean defaultValue = false;

        obj.setBoolean("bool_key", value);

        assertEquals(value, obj.getBoolean("bool_key"));
        assertEquals(defaultValue, obj.getBoolean("bool_key_not-present", defaultValue));
    }

    // Verify set and get string methods work as intended
    @Test
    public void testSetAndGetString() {
        String value = "Some string";
        String defaultValue = "Some default string";

        obj.setString("string_key", value);

        assertEquals(value, obj.getString("string_key"));
        assertEquals(defaultValue, obj.getString("string_key_not-present", defaultValue));
    }

    // Verify getInt method with non-existent key throws exception
    @Test(expected = RuntimeException.class)
    public void testGetNonExistentKeyInt() {
        obj.getInt("nonexistent");
    }

    // Verify getLong method with non-existent key throws exception
    @Test(expected = RuntimeException.class)
    public void testGetNonExistentKeyLong() {
        obj.getLong("nonexistent");
    }

    // Verify getFloat method with non-existent key throws exception
    @Test(expected = RuntimeException.class)
    public void testGetNonExistentKeyFloat() {
        obj.getFloat("nonexistent");
    }

    // Verify getDouble method with non-existent key throws exception
    @Test(expected = RuntimeException.class)
    public void testGetNonExistentKeyDouble() {
        obj.getDouble("nonexistent");
    }

    // Verify getBoolean method with non-existent key throws exception
    @Test(expected = RuntimeException.class)
    public void testGetNonExistentKeyBoolean() {
        obj.getBoolean("nonexistent");
    }

    // Verify getString method with non-existent returns null
    @Test
    public void testGetNonExistentKeyString() {
        assertNull(obj.getString("nonexistent"));
    }

    // Verify stringToValue with empty string works as intended
    @Test
    public void testStringToValueEmptyString() {
        assertEquals("", JSONObject.stringToValue(""));
    }

    // Verify stringToValue with boolean strings work as intended
    @Test
    public void testStringToValueBoolean() {
        assertEquals(Boolean.TRUE, JSONObject.stringToValue("true"));
        assertEquals(Boolean.FALSE, JSONObject.stringToValue("FALSE"));
        assertEquals(Boolean.TRUE, JSONObject.stringToValue("True"));
    }

    // Verify stringToValue with 'null' strings work as intended
    @Test
    public void testStringToValueNull() {
        assertEquals(JSONObject.NULL, JSONObject.stringToValue("null"));
        assertEquals(JSONObject.NULL, JSONObject.stringToValue("NULL"));
        assertEquals(JSONObject.NULL, JSONObject.stringToValue("Null"));
    }

    // Verify stringToValue with int strings work as intended
    @Test
    public void testStringToValueInteger() {
        assertEquals(42, JSONObject.stringToValue("42"));
        assertEquals(-17, JSONObject.stringToValue("-17"));
        assertEquals(42, JSONObject.stringToValue("+42"));
        assertEquals(Integer.MAX_VALUE, JSONObject.stringToValue("2147483647"));
        assertEquals(Integer.MIN_VALUE, JSONObject.stringToValue("-2147483648"));
    }

    // Verify stringToValue with long strings work as intended
    @Test
    public void testStringToValueLong() {
        assertEquals(2147483648L, JSONObject.stringToValue("2147483648"));
        assertEquals(-2147483649L, JSONObject.stringToValue("-2147483649"));
        assertEquals(Long.MAX_VALUE, JSONObject.stringToValue("9223372036854775807"));
        assertEquals(Long.MIN_VALUE, JSONObject.stringToValue("-9223372036854775808"));
    }

    // Verify stringToValue with double strings work as intended
    @Test
    public void testStringToValueDouble() {
        assertEquals(3.14, JSONObject.stringToValue("3.14"));
        assertEquals(-0.5, JSONObject.stringToValue("-0.5"));
        assertEquals(0.5, JSONObject.stringToValue(".5"));
        assertEquals(-0.5, JSONObject.stringToValue("-.5"));
        assertEquals(0.5, JSONObject.stringToValue("+.5"));
    }

    // Verify stringToValue with scientific notation strings work as intended
    @Test
    public void testStringToValueScientificNotation() {
        assertEquals(100000.0, JSONObject.stringToValue("1e5"));
        assertEquals(100000.0, JSONObject.stringToValue("1E5"));
        assertEquals(0.00001, JSONObject.stringToValue("1e-5"));
        assertEquals(0.00001, JSONObject.stringToValue("1E-5"));
        assertEquals(-100000.0, JSONObject.stringToValue("-1e5"));
        assertEquals(100000.0, JSONObject.stringToValue("+1e5"));
    }

    // Verify stringToValue with special numbers work as intended
    @Test
    public void testStringToValueSpecialNumbers() {
        assertEquals("NaN", JSONObject.stringToValue("NaN"));
        assertEquals("Infinity", JSONObject.stringToValue("Infinity"));
        assertEquals("-Infinity", JSONObject.stringToValue("-Infinity"));
    }

    // Verify stringToValue with invalid numbers work as intended
    @Test
    public void testStringToValueInvalidNumbers() {
        assertEquals("0x123", JSONObject.stringToValue("0x123")); // Hexadecimal
        assertEquals("0b101", JSONObject.stringToValue("0b101")); // Binary
        assertEquals("1.2.3", JSONObject.stringToValue("1.2.3")); // Multiple dots
        assertEquals("1e2e3", JSONObject.stringToValue("1e2e3")); // Multiple exponents
        assertEquals("--1", JSONObject.stringToValue("--1")); // Multiple signs
        assertEquals("++1", JSONObject.stringToValue("++1")); // Multiple signs
        assertEquals("1e", JSONObject.stringToValue("1e")); // Incomplete exponent
        assertEquals("e1", JSONObject.stringToValue("e1")); // Missing base
        assertEquals(".e1", JSONObject.stringToValue(".e1")); // Missing base with dot
    }

    // Verify stringToValue with regular strings work as intended
    @Test
    public void testStringToValueRegularStrings() {
        assertEquals("hello", JSONObject.stringToValue("hello"));
        assertEquals("123abc", JSONObject.stringToValue("123abc"));
        assertEquals("true_false", JSONObject.stringToValue("true_false"));
        assertEquals("null_value", JSONObject.stringToValue("null_value"));
    }

    // Verify valueToString with null values work as intended
    @Test
    public void testValueToStringNull() {
        assertEquals("null", JSONObject.valueToString(null));
        assertEquals("null", JSONObject.valueToString(JSONObject.NULL));
    }

    // Verify valueToString with numbers work as intended
    @Test
    public void testValueToStringNumbers() {
        assertEquals("42", JSONObject.valueToString(42));
        assertEquals("-17", JSONObject.valueToString(-17));
        assertEquals("3.14159", JSONObject.valueToString(3.14159));
        assertEquals("1.23E-4", JSONObject.valueToString(0.000123));
        assertEquals("9223372036854775807", JSONObject.valueToString(Long.MAX_VALUE));
    }

    // Verify valueToString with booleans work as intended
    @Test
    public void testValueToStringBoolean() {
        assertEquals("true", JSONObject.valueToString(true));
        assertEquals("false", JSONObject.valueToString(false));
        assertEquals("true", JSONObject.valueToString(Boolean.TRUE));
        assertEquals("false", JSONObject.valueToString(Boolean.FALSE));
    }

    // Verify valueToString with JSONArray objects work as intended
    @Test
    public void testValueToStringJSONArray() {
        JSONArray arr = new JSONArray();
        arr.append(1);
        arr.append("test");
        assertEquals("[\n  1,\n  \"test\"\n]", JSONObject.valueToString(arr));
    }

    // Verify valueToString with array objects work as intended
    @Test
    public void testValueToStringArray() {
        String[] arr = new String[]{"test1", "test2"};
        assertEquals("[\n  \"test1\",\n  \"test2\"\n]", JSONObject.valueToString(arr));

        int[] intArr = new int[]{1, 2, 3};
        assertEquals("[\n  1,\n  2,\n  3\n]", JSONObject.valueToString(intArr));
    }

    // Verify valueToString with escaped characters work as intended
    @Test
    public void testValueToStringRegularObject() {
        assertEquals("\"hello\"", JSONObject.valueToString("hello"));
        assertEquals("\"special\\\"quote\\\"\"",
                JSONObject.valueToString("special\"quote\""));
        assertEquals("\"tab\\tand\\nnewline\"",
                JSONObject.valueToString("tab\tand\nnewline"));
    }

    // Verify valueToString with invalid numbers throw exception
    @Test(expected = RuntimeException.class)
    public void testValueToStringInvalidNumber() {
        JSONObject.valueToString(Double.POSITIVE_INFINITY);
    }

    // Verify valueToString with NaN throws exception
    @Test(expected = RuntimeException.class)
    public void testValueToStringNaN() {
        JSONObject.valueToString(Double.NaN);
    }
}