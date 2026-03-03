package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("image")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageTest extends VisualTest {

    @Test
    @Order(1)
    @DisplayName("Draw PImage")
    public void testDrawImage() {
        assertVisualMatch("shapes/image-draw", new ProcessingSketch() {
            PImage img;
            @Override
            public void setup(PApplet p) {
                img = p.createImage(20, 20, PApplet.RGB);
                img.loadPixels();
                for (int i = 0; i < img.pixels.length; i++) {
                    img.pixels[i] = p.color(255, 0, 0);
                }
                img.updatePixels();
            }
            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.image(img, 10, 10);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @DisplayName("Tint image")
    public void testTintImage() {
        assertVisualMatch("shapes/image-tint", new ProcessingSketch() {
            PImage img;
            @Override
            public void setup(PApplet p) {
                img = p.createImage(20, 20, PApplet.RGB);
                img.loadPixels();
                for (int i = 0; i < img.pixels.length; i++) {
                    img.pixels[i] = p.color(255);
                }
                img.updatePixels();
            }
            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.tint(0, 150, 255);
                p.image(img, 5, 5);
                p.noTint();
                p.image(img, 25, 25);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(3)
    @DisplayName("Resize image")
    public void testResizeImage() {
        assertVisualMatch("shapes/image-resize", new ProcessingSketch() {
            PImage img;
            @Override
            public void setup(PApplet p) {
                img = p.createImage(10, 10, PApplet.RGB);
                img.loadPixels();
                for (int i = 0; i < img.pixels.length; i++) {
                    img.pixels[i] = p.color(0, 200, 100);
                }
                img.updatePixels();
            }
            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.image(img, 5, 5, 40, 40);
            }
        }, new TestConfig(50, 50));
    }
}