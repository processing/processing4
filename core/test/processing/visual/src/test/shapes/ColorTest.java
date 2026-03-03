package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("color")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ColorTest extends VisualTest {

    @Test
    @Order(1)
    @DisplayName("Fill with RGB color")
    public void testFillRGB() {
        assertVisualMatch("shapes/color-fill-rgb", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.noStroke();
            }
            @Override
            public void draw(PApplet p) {
                p.fill(255, 0, 0);
                p.rect(5, 5, 15, 40);
                p.fill(0, 255, 0);
                p.rect(20, 5, 15, 40);
                p.fill(0, 0, 255);
                p.rect(35, 5, 10, 40);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @DisplayName("Fill with alpha transparency")
    public void testFillAlpha() {
        assertVisualMatch("shapes/color-fill-alpha", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.noStroke();
            }
            @Override
            public void draw(PApplet p) {
                p.fill(255, 0, 0);
                p.rect(5, 5, 30, 40);
                p.fill(0, 0, 255, 128);
                p.rect(15, 5, 30, 40);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(3)
    @DisplayName("Stroke color")
    public void testStrokeColor() {
        assertVisualMatch("shapes/color-stroke", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.strokeWeight(3);
                p.noFill();
            }
            @Override
            public void draw(PApplet p) {
                p.stroke(255, 0, 0);
                p.rect(5, 5, 15, 15);
                p.stroke(0, 255, 0);
                p.rect(25, 5, 15, 15);
                p.stroke(0, 0, 255);
                p.rect(5, 25, 15, 15);
                p.stroke(255, 165, 0);
                p.rect(25, 25, 15, 15);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(4)
    @DisplayName("HSB color mode")
    public void testHSBColorMode() {
        assertVisualMatch("shapes/color-hsb", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.noStroke();
                p.colorMode(PApplet.HSB, 360, 100, 100);
            }
            @Override
            public void draw(PApplet p) {
                for (int i = 0; i < 5; i++) {
                    p.fill(i * 72, 80, 90);
                    p.rect(i * 10, 10, 10, 30);
                }
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(5)
    @DisplayName("Background color")
    public void testBackgroundColor() {
        assertVisualMatch("shapes/color-background", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.noStroke();
            }
            @Override
            public void draw(PApplet p) {
                p.background(100, 150, 200);
                p.fill(255);
                p.rect(10, 10, 30, 30);
            }
        }, new TestConfig(50, 50));
    }
}

