package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("rendering")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShapeTest extends VisualTest {

    // Helper method for common setup
    private ProcessingSketch createShapeTest(ShapeDrawingCallback callback) {
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
    interface ShapeDrawingCallback {
        void draw(PApplet p);
    }

    // ========== Polylines ==========

    @Test
    @Order(1)
    @Tag("polylines")
    @DisplayName("Drawing polylines")
    public void testPolylines() {
        assertVisualMatch("shapes/polylines", createShapeTest(p -> {
            p.beginShape();
            p.vertex(10, 10);
            p.vertex(15, 40);
            p.vertex(40, 35);
            p.vertex(25, 15);
            p.vertex(15, 25);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @Tag("polylines")
    @DisplayName("Drawing closed polylines")
    public void testClosedPolylines() {
        assertVisualMatch("shapes/closed-polylines", createShapeTest(p -> {
            p.beginShape();
            p.vertex(10, 10);
            p.vertex(15, 40);
            p.vertex(40, 35);
            p.vertex(25, 15);
            p.vertex(15, 25);
            p.endShape(PApplet.CLOSE);
        }), new TestConfig(50, 50));
    }

    // ========== Contours ==========

    @Test
    @Order(3)
    @Tag("contours")
    @DisplayName("Drawing with contours")
    public void testContours() {
        assertVisualMatch("shapes/contours", createShapeTest(p -> {
            p.beginShape();
            // Outer circle
            vertexCircle(p, 15, 15, 10, 1);

            // Inner cutout
            p.beginContour();
            vertexCircle(p, 15, 15, 5, -1);
            p.endContour();

            // Second outer shape
            p.beginContour();
            vertexCircle(p, 30, 30, 8, -1);
            p.endContour();

            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(4)
    @Tag("contours")
    @DisplayName("Drawing with a single closed contour")
    public void testSingleClosedContour() {
        assertVisualMatch("shapes/single-closed-contour", createShapeTest(p -> {
            p.beginShape();
            p.vertex(10, 10);
            p.vertex(40, 10);
            p.vertex(40, 40);
            p.vertex(10, 40);

            p.beginContour();
            p.vertex(20, 20);
            p.vertex(20, 30);
            p.vertex(30, 30);
            p.vertex(30, 20);
            p.endContour();

            p.endShape(PApplet.CLOSE);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(5)
    @Tag("contours")
    @DisplayName("Drawing with a single unclosed contour")
    public void testSingleUnclosedContour() {
        assertVisualMatch("shapes/single-unclosed-contour", createShapeTest(p -> {
            p.beginShape();
            p.vertex(10, 10);
            p.vertex(40, 10);
            p.vertex(40, 40);
            p.vertex(10, 40);

            p.beginContour();
            p.vertex(20, 20);
            p.vertex(20, 30);
            p.vertex(30, 30);
            p.vertex(30, 20);
            p.endContour();

            p.endShape(PApplet.CLOSE);
        }), new TestConfig(50, 50));
    }

    // ========== Triangle Shapes ==========

    @Test
    @Order(6)
    @Tag("triangles")
    @DisplayName("Drawing triangle fans")
    public void testTriangleFans() {
        assertVisualMatch("shapes/triangle-fans", createShapeTest(p -> {
            p.beginShape(PApplet.TRIANGLE_FAN);
            p.vertex(25, 25);
            for (int i = 0; i <= 12; i++) {
                float angle = PApplet.map(i, 0, 12, 0, PApplet.TWO_PI);
                p.vertex(25 + 10 * PApplet.cos(angle), 25 + 10 * PApplet.sin(angle));
            }
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(7)
    @Tag("triangles")
    @DisplayName("Drawing triangle strips")
    public void testTriangleStrips() {
        assertVisualMatch("shapes/triangle-strips", createShapeTest(p -> {
            p.beginShape(PApplet.TRIANGLE_STRIP);
            p.vertex(10, 10);
            p.vertex(30, 10);
            p.vertex(15, 20);
            p.vertex(35, 20);
            p.vertex(10, 40);
            p.vertex(30, 40);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(8)
    @Tag("triangles")
    @DisplayName("Drawing with triangles")
    public void testTriangles() {
        assertVisualMatch("shapes/triangles", createShapeTest(p -> {
            p.beginShape(PApplet.TRIANGLES);
            p.vertex(10, 10);
            p.vertex(15, 40);
            p.vertex(40, 35);
            p.vertex(25, 15);
            p.vertex(10, 10);
            p.vertex(15, 25);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    // ========== Quad Shapes ==========

    @Test
    @Order(9)
    @Tag("quads")
    @DisplayName("Drawing quad strips")
    public void testQuadStrips() {
        assertVisualMatch("shapes/quad-strips", createShapeTest(p -> {
            p.beginShape(PApplet.QUAD_STRIP);
            p.vertex(10, 10);
            p.vertex(30, 10);
            p.vertex(15, 20);
            p.vertex(35, 20);
            p.vertex(10, 40);
            p.vertex(30, 40);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(10)
    @Tag("quads")
    @DisplayName("Drawing with quads")
    public void testQuads() {
        assertVisualMatch("shapes/quads", createShapeTest(p -> {
            p.beginShape(PApplet.QUADS);
            p.vertex(10, 10);
            p.vertex(15, 10);
            p.vertex(15, 15);
            p.vertex(10, 15);
            p.vertex(25, 25);
            p.vertex(30, 25);
            p.vertex(30, 30);
            p.vertex(25, 30);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    // ========== Curves ==========

    @Test
    @Order(11)
    @Tag("curves")
    @DisplayName("Drawing with curves")
    public void testCurves() {
        assertVisualMatch("shapes/curves", createShapeTest(p -> {
            p.beginShape();
            p.curveVertex(10, 10);
            p.curveVertex(15, 40);
            p.curveVertex(40, 35);
            p.curveVertex(25, 15);
            p.curveVertex(15, 25);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(12)
    @Tag("curves")
    @DisplayName("Drawing closed curves")
    public void testClosedCurves() {
        assertVisualMatch("shapes/closed-curves", createShapeTest(p -> {
            p.beginShape();
            p.curveVertex(10, 10);
            p.curveVertex(15, 40);
            p.curveVertex(40, 35);
            p.curveVertex(25, 15);
            p.curveVertex(15, 25);
            p.endShape(PApplet.CLOSE);
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(13)
    @Tag("curves")
    @DisplayName("Drawing with curves with tightness")
    public void testCurvesWithTightness() {
        assertVisualMatch("shapes/curves-tightness", createShapeTest(p -> {
            p.curveTightness(-1);
            p.beginShape();
            p.curveVertex(10, 10);
            p.curveVertex(15, 40);
            p.curveVertex(40, 35);
            p.curveVertex(25, 15);
            p.curveVertex(15, 25);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    // ========== Bezier Curves ==========

    @Test
    @Order(14)
    @Tag("bezier")
    @DisplayName("Drawing with bezier curves")
    public void testBezierCurves() {
        assertVisualMatch("shapes/bezier-curves", createShapeTest(p -> {
            p.beginShape();
            p.vertex(10, 10);
            p.bezierVertex(10, 40, 40, 40, 40, 10);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(15)
    @Tag("bezier")
    @DisplayName("Drawing with quadratic beziers")
    public void testQuadraticBeziers() {
        assertVisualMatch("shapes/quadratic-beziers", createShapeTest(p -> {
            p.beginShape();
            p.vertex(10, 10);
            p.quadraticVertex(25, 40, 40, 10);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    // ========== Points and Lines ==========

    @Test
    @Order(16)
    @Tag("primitives")
    @DisplayName("Drawing with points")
    public void testPoints() {
        assertVisualMatch("shapes/points", createShapeTest(p -> {
            p.strokeWeight(5);
            p.beginShape(PApplet.POINTS);
            p.vertex(10, 10);
            p.vertex(15, 40);
            p.vertex(40, 35);
            p.vertex(25, 15);
            p.vertex(15, 25);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    @Test
    @Order(17)
    @Tag("primitives")
    @DisplayName("Drawing with lines")
    public void testLines() {
        assertVisualMatch("shapes/lines", createShapeTest(p -> {
            p.beginShape(PApplet.LINES);
            p.vertex(10, 10);
            p.vertex(15, 40);
            p.vertex(40, 35);
            p.vertex(25, 15);
            p.endShape();
        }), new TestConfig(50, 50));
    }

    // ========== Helper Methods ==========

    /**
     * Helper method to create a circle using vertices
     */
    private void vertexCircle(PApplet p, float x, float y, float r, int direction) {
        for (int i = 0; i <= 12; i++) {
            float angle = PApplet.map(i, 0, 12, 0, PApplet.TWO_PI) * direction;
            p.vertex(x + r * PApplet.cos(angle), y + r * PApplet.sin(angle));
        }
    }
}