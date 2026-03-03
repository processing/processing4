package processing;
import org.junit.Test;
import static org.junit.Assert.*;
import processing.core.PGraphics;

public class ShapeTests {

    @Test
    public void testCanvasWidthAfterSetSize() {
        // Create a PGraphics object and set its size
        PGraphics pg = new PGraphics();
        pg.setSize(200, 100); // canvas size

        pg.beginDraw();
        pg.rect(10, 10, 100, 50); // draw a rectangle
        pg.endDraw();

        // Assert that the canvas width is 200 (not the rect width)
        assertEquals(200, pg.width);
    }

    @Test
    public void testCanvasHeightAfterSetSize() {
        // Create a PGraphics object and set its size
        PGraphics pg = new PGraphics();
        pg.setSize(300, 150); // canvas size

        pg.beginDraw();
        pg.rect(20, 20, 50, 25); // draw another rectangle
        pg.endDraw();

        // Assert that the canvas height is 150
        assertEquals(150, pg.height);
    }
}