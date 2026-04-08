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

}