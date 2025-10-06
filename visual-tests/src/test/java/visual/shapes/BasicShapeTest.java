package visual.shapes;


import org.junit.jupiter.api.*;
import processing.core.*;
import visual.base.*;
import processing.test.visual.*;

@Tag("basic")
@Tag("shapes")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicShapeTest extends VisualTest {

    @Test
    @Order(1)
    @DisplayName("Red circle renders correctly")
    public void testRedCircle() {
        assertVisualMatch("basic-shapes/red-circle", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.noStroke();
            }

            @Override
            public void draw(PApplet p) {
                p.background(255, 255, 255);
                p.fill(255, 0, 0);
                p.ellipse(p.width/2, p.height/2, 100, 100);
            }
        });
    }

    @Test
    @Order(2)
    @DisplayName("Blue square renders correctly")
    public void testBlueSquare() {
        assertVisualMatch("basic-shapes/blue-square", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.stroke(0);
                p.strokeWeight(2);
            }

            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.fill(0, 0, 255);
                p.rect(p.width/2 - 50, p.height/2 - 50, 100, 100);
            }
        });
    }

    @Test
    @Order(3)
    @DisplayName("Green circle renders correctly")
    public void testGreenCircle() {
        assertVisualMatch("basic-shapes/green-circle", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.noStroke();
            }

            @Override
            public void draw(PApplet p) {
                p.background(255, 255, 255);
                p.fill(0, 255, 0);
                p.ellipse(p.width/2, p.height/2, 100, 100);
            }
        });
    }

    @Test
    @Order(4)
    @DisplayName("Custom size canvas")
    public void testCustomSize() {
        TestConfig config = new TestConfig(600, 400);

        assertVisualMatch("basic-shapes/custom-size-rect", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.noStroke();
            }

            @Override
            public void draw(PApplet p) {
                p.background(240, 240, 240);
                p.fill(128, 0, 128);
                p.rect(50, 50, p.width - 100, p.height - 100);
            }
        }, config);
    }
}