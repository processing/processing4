package processing.core;

import java.awt.Font;         //Javas built in font class
import org.junit.Test;        //Using Junit4 test implementation
import static org.junit.Assert.assertEquals;    //To compare expected vs actual values

public class PFontTest {

    @Test
    public void getsFontNameCorrectly() {
        Font trueFont = new Font("Name", Font.PLAIN, 16);
        PFont font = new PFont(trueFont, true, null);
        assertEquals("Name", font.getName());
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
