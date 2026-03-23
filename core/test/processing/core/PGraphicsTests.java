package processing.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import processing.core.PGraphics;

public class PGraphicsTests {

    @Test
    public void testCanvasSizeAfterSetSize() {
        // Create a PGraphics object and set its size
        PGraphics pg = new PGraphics();
        pg.setSize(200, 150);

        // Assert that both width and height are correctly initialized
        assertEquals(200, pg.width);
        assertEquals(150, pg.height);
    }
}