package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("3d")
@Tag("p3d")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Shape3DTest extends VisualTest {

    private ProcessingSketch create3DTest(Shape3DCallback callback) {
        return new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                // P3D mode setup would go here if supported
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
    interface Shape3DCallback {
        void draw(PApplet p);
    }

    @Test
    @DisplayName("3D vertex coordinates")
    public void test3DVertexCoordinates() {
        assertVisualMatch("shapes-3d/vertex-coordinates", create3DTest(p -> {
            p.beginShape(PApplet.QUAD_STRIP);
            p.vertex(10, 10, 0);
            p.vertex(10, 40, -150);
            p.vertex(40, 10, 150);
            p.vertex(40, 40, 200);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @DisplayName("Per-vertex fills")
    public void testPerVertexFills() {
        assertVisualMatch("shapes-3d/per-vertex-fills", create3DTest(p -> {
            p.beginShape(PApplet.QUAD_STRIP);
            p.fill(0);
            p.vertex(10, 10);
            p.fill(255, 0, 0);
            p.vertex(45, 5);
            p.fill(0, 255, 0);
            p.vertex(15, 35);
            p.fill(255, 255, 0);
            p.vertex(40, 45);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @DisplayName("Per-vertex strokes")
    public void testPerVertexStrokes() {
        assertVisualMatch("shapes-3d/per-vertex-strokes", create3DTest(p -> {
            p.strokeWeight(5);
            p.beginShape(PApplet.QUAD_STRIP);
            p.stroke(0);
            p.vertex(10, 10);
            p.stroke(255, 0, 0);
            p.vertex(45, 5);
            p.stroke(0, 255, 0);
            p.vertex(15, 35);
            p.stroke(255, 255, 0);
            p.vertex(40, 45);
            p.endShape();
        }), new TestConfig(50, 50));
    }
}