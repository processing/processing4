package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("transforms")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransformTest extends VisualTest {

    @Test
    @Order(1)
    @DisplayName("Translate")
    public void testTranslate() {
        assertVisualMatch("shapes/transform-translate", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }
            @Override
            public void draw(PApplet p) {
                p.translate(20, 20);
                p.rect(0, 0, 20, 20);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @DisplayName("Rotate")
    public void testRotate() {
        assertVisualMatch("shapes/transform-rotate", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }
            @Override
            public void draw(PApplet p) {
                p.translate(25, 25);
                p.rotate(PApplet.PI / 4);
                p.rect(-10, -10, 20, 20);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(3)
    @DisplayName("Scale")
    public void testScale() {
        assertVisualMatch("shapes/transform-scale", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }
            @Override
            public void draw(PApplet p) {
                p.scale(1.5f);
                p.rect(5, 5, 20, 20);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(4)
    @DisplayName("Push and Pop Matrix")
    public void testPushPopMatrix() {
        assertVisualMatch("shapes/transform-pushpop", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }
            @Override
            public void draw(PApplet p) {
                p.pushMatrix();
                p.translate(10, 10);
                p.rotate(PApplet.PI / 6);
                p.rect(0, 0, 15, 15);
                p.popMatrix();
                p.fill(255, 0, 0);
                p.rect(25, 25, 15, 15);
            }
        }, new TestConfig(50, 50));
    }
}