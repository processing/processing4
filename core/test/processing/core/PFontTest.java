package processing.core;

import java.awt.Font;         //Javas built in font class
import org.junit.Test;        //Using Junit4 test implementation
import static org.junit.Assert.assertEquals;    //To compare expected vs actual values

public class PFontTest {

    @Test
    public void constructor_setsSizeCorrectly() {
        Font awtFont = new Font("Dialog", Font.PLAIN, 16);  //Truth, what PFont size should be
        PFont font = new PFont(awtFont, true, null); //call to constructor w specific values
        assertEquals(16, font.getSize()); //actual test, expects 16 compares against actual value
    }

    @Test
    public void test_psname_getCorrectName() {
        Font awtFont = new Font("Dialog", Font.PLAIN, 16);  //Truth, what PFont size should be
        PFont font = new PFont(awtFont, true, null);
        assertEquals(awtFont.getPSName(), font.getPostScriptName()); //test, expecting
    }

    @Test
    public void test_size_getCorrectSize() {
        Font awtFont = new Font("Dialog", Font.PLAIN, 16);  //Truth, what PFont size should be
        PFont font = new PFont(awtFont, true, null);
        assertEquals(awtFont.getSize(), font.getSize()); //test, expecting
    }
}
