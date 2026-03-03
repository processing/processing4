package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("primitives")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimitiveShapeTest extends VisualTest {

    private ProcessingSketch createTest(ShapeCallback callback) {
        return new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }

            @Override
            public void draw(PApplet p) {
                callback.draw(p);
            }
        };
    }

    @FunctionalInterface
    interface ShapeCallback {
        void draw(PApplet p);
    }

    @Test
    @Order(1)
    @DisplayName("Drawing a rectangle")
    public void testRect() {
        assertVisualMatch("shapes/rect", createTest(p -> {
            p.rect(10, 10, 30, 30);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @DisplayName("Drawing an ellipse")
    public void testEllipse() {
        assertVisualMatch("shapes/ellipse", createTest(p -> {
            p.ellipse(25, 25, 30, 20);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(3)
    @DisplayName("Drawing a triangle")
    public void testTriangle() {
        assertVisualMatch("shapes/triangle", createTest(p -> {
            p.triangle(25, 10, 10, 40, 40, 40);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(4)
    @DisplayName("Drawing an arc")
    public void testArc() {
        assertVisualMatch("shapes/arc", createTest(p -> {
            p.arc(25, 25, 30, 30, 0, PApplet.PI);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(5)
    @DisplayName("Drawing a line")
    public void testLine() {
        assertVisualMatch("shapes/line", createTest(p -> {
            p.line(10, 10, 40, 40);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(6)
    @DisplayName("Drawing a point")
    public void testPoint() {
        assertVisualMatch("shapes/point", createTest(p -> {
            p.strokeWeight(5);
            p.point(25, 25);
        }), new TestConfig(50, 50));
    }
}