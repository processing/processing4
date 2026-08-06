package processing.data;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class JSONArrayTest {
  @Test
  public void testDefaultConstructor() {
    JSONArray jsonArray = new JSONArray();
    assertEquals(0, jsonArray.size());
  }

  @Test
  public void testEmptyTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[]"));
    assertEquals(0, jsonArray.size());
  }

  @Test
  public void testNullTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[null]"));
    assertEquals(1, jsonArray.size());
    assertEquals(JSONObject.NULL, jsonArray.get(0));
  }

  @Test
  public void testIntTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[1]"));
    assertEquals(1, jsonArray.size());
    assertEquals(1, jsonArray.get(0));
  }

  @Test
  public void testDoubleTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[0.1]"));
    assertEquals(1, jsonArray.size());
    assertEquals(0.1, jsonArray.get(0));
  }

  @Test
  public void testStringTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[\"a\"]"));
    assertEquals(1, jsonArray.size());
    assertEquals("a", jsonArray.get(0));
  }

  @Test
  public void testArrayTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[[1]]"));
    assertEquals(1, jsonArray.size());
    assertEquals(JSONArray.class, jsonArray.get(0).getClass());
    JSONArray innerJsonArray = ((JSONArray) jsonArray.get(0));
    assertEquals(1, innerJsonArray.size());
    assertEquals(1, innerJsonArray.get(0));
  }

  @Test
  public void testObjectTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[{\"a\":\"b\"}]"));
    assertEquals(1, jsonArray.size());
    assertEquals(JSONObject.class, jsonArray.get(0).getClass());
    JSONObject innerJsonObject = ((JSONObject) jsonArray.get(0));
    assertEquals(1, innerJsonObject.size());
    assertEquals("b", innerJsonObject.get("a"));
  }

  @Test
  public void testMixedTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[null, 1, 2147483648, 0.1, \"a\", [1], {\"a\": \"b\"}]"));
    assertEquals(7, jsonArray.size());

    assertEquals(JSONObject.NULL, jsonArray.get(0));

    assertEquals(1, jsonArray.get(1));

    assertEquals(2147483648L, jsonArray.get(2));

    assertEquals(0.1, jsonArray.get(3));

    assertEquals("a", jsonArray.get(4));

    assertEquals(JSONArray.class, jsonArray.get(5).getClass());
    JSONArray innerJsonArray = (JSONArray) jsonArray.get(5);
    assertEquals(1, innerJsonArray.size());
    assertEquals(1, innerJsonArray.get(0));

    assertEquals(JSONObject.class, jsonArray.get(6).getClass());
    JSONObject innerJsonObject = (JSONObject) jsonArray.get(6);
    assertEquals(1, innerJsonObject.size());
    assertEquals("b", innerJsonObject.get("a"));
  }

  @Test
  public void testTrailingCommaTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[1, 2,]"));
    assertEquals(2, jsonArray.size());
    assertEquals(1, jsonArray.get(0));
    assertEquals(2, jsonArray.get(1));
  }

  @Test
  public void testWhitespaceTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener(" [ 1 , 2 , ] "));
    assertEquals(2, jsonArray.size());
    assertEquals(1, jsonArray.get(0));
    assertEquals(2, jsonArray.get(1));
  }

  @Test
  public void testNoSpacesTokenerConstructor() {
    JSONArray jsonArray = new JSONArray(new JSONTokener("[1,2]"));
    assertEquals(2, jsonArray.size());
    assertEquals(1, jsonArray.get(0));
    assertEquals(2, jsonArray.get(1));
  }

  @Test
  public void testNoStartTokenerConstructorThrowsException() {
    RuntimeException exception = assertThrows(RuntimeException.class, () -> new JSONArray(new JSONTokener("1, 2]")));
    assertEquals("A JSONArray text must start with '['", exception.getMessage());
  }

  @Test
  public void testNoEndTokenerConstructorThrowsException() {
    RuntimeException exception = assertThrows(RuntimeException.class, () -> new JSONArray(new JSONTokener("[1, 2")));
    assertEquals("Expected a ',' or ']'", exception.getMessage());
  }

  @Test
  public void testEmptyIntListConstructor() {
    JSONArray jsonArray = new JSONArray(new IntList(new int[]{}));
    assertEquals(0, jsonArray.size());
  }

  @Test
  public void testFilledIntListConstructor() {
    JSONArray jsonArray = new JSONArray(new IntList(new int[]{1, 2}));
    assertEquals(2, jsonArray.size());
    assertEquals(1, jsonArray.get(0));
    assertEquals(2, jsonArray.get(1));
  }

  @Test
  public void testEmptyFloatListConstructor() {
    JSONArray jsonArray = new JSONArray(new FloatList(new float[]{}));
    assertEquals(0, jsonArray.size());
  }

  @Test
  public void testFilledFloatListConstructor() {
    JSONArray jsonArray = new JSONArray(new FloatList(new float[]{0.1f, 0.2f}));
    assertEquals(2, jsonArray.size());
    assertEquals(0.1f, jsonArray.get(0));
    assertEquals(0.2f, jsonArray.get(1));
  }

  @Test
  public void testEmptyStringListConstructor() {
    JSONArray jsonArray = new JSONArray(new StringList(new String[]{}));
    assertEquals(0, jsonArray.size());
  }

  @Test
  public void testFilledStringListConstructor() {
    JSONArray jsonArray = new JSONArray(new StringList(new String[]{"a", "b"}));
    assertEquals(2, jsonArray.size());
    assertEquals("a", jsonArray.get(0));
    assertEquals("b", jsonArray.get(1));
  }

  @Test
  public void testEmptyArrayConstructor() {
    JSONArray jsonArray = new JSONArray(new int[]{});
    assertEquals(0, jsonArray.size());
  }

  @Test
  public void testFilledArrayConstructor() {
    JSONArray jsonArray = new JSONArray(new int[]{1, 2});
    assertEquals(2, jsonArray.size());
    assertEquals(1, jsonArray.get(0));
    assertEquals(2, jsonArray.get(1));
  }

  @Test
  public void testNullFormat() {
    String json = """
            [
            null,
            null
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testIntFormat() {
    String json = """
            [
            1,
            2
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testLongFormat() {
    String json = """
            [
            2147483648,
            2147483649
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testDoubleFormat() {
    String json = """
            [
            0.1,
            0.2
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testJSONArrayFormat() {
    String json = """
            [
            [],
            [
            1,
            2
            ]
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testJSONObjectFormat() {
    String json = """
            [
            null,
            1,
            2147483648,
            0.1,
            [
            1,
            2
            ],
            {
            "a":"b",
            "c":"d"
            }
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testMixedFormat() {
    String json = """
            [
            null,
            1,
            2147483648,
            0.1,
            [
            1,
            2
            ],
            {
            "a":"b",
            "c":"d"
            }
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(0);
    assertEquals(json, output);
  }

  @Test
  public void testIndentFormat() {
    String json = """
            [
                1,
                2,
                [
                    1,
                    2
                ]
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(4);
    assertEquals(json, output);
  }

  @Test
  public void testNegativeIndentFormat() {
    String json = "[1,2,[1,2]]";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    String output = jsonArray.format(-1);
    assertEquals(json, output);
  }

  @Test
  public void testIndentWrite() {
    String json = """
            [
              1,
              2,
              [
                1,
                2
              ]
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonArray.write(new PrintWriter(byteArrayOutputStream));
    assertEquals(json, byteArrayOutputStream.toString());
  }

  @Test
  public void testIndentOptionsWrite() {
    String json = """
            [
                1,
                2,
                [
                    1,
                    2
                ]
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonArray.write(new PrintWriter(byteArrayOutputStream), "indent=4");
    assertEquals(json, byteArrayOutputStream.toString());
  }

  @Test
  public void testCompactOptionsWrite() {
    String json = "[1,2,[1,2]]";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonArray.write(new PrintWriter(byteArrayOutputStream), "compact");
    assertEquals(json, byteArrayOutputStream.toString());
  }

  @Test
  public void testMultiOptionsWrite() {
    String json = """
            [
                1,
                2,
                [
                    1,
                    2
                ]
            ]""";
    JSONArray jsonArray = new JSONArray(new JSONTokener(json));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonArray.write(new PrintWriter(byteArrayOutputStream), "indent=2,indent=4");
    assertEquals(json, byteArrayOutputStream.toString());
  }
}
