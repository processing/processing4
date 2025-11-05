package processing.visual.src.test.shapemodes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("modes")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShapeModeTest extends VisualTest {

    /**
     * Helper function that draws a shape using the specified shape mode
     * @param p The PApplet instance
     * @param shape The shape to draw: "ellipse", "arc", or "rect"
     * @param mode The mode constant (CORNERS, CORNER, CENTER, or RADIUS)
     * @param x1 First x coordinate
     * @param y1 First y coordinate
     * @param x2 Second x/width coordinate
     * @param y2 Second y/height coordinate
     */
    private void shapeCorners(PApplet p, String shape, int mode, float x1, float y1, float x2, float y2) {
        // Adjust coordinates for testing modes other than CORNERS
        if (mode == PApplet.CORNER) {
            // Find top left corner
            float x = PApplet.min(x1, x2);
            float y = PApplet.min(y1, y2);
            // Calculate width and height
            // Don't use abs(), so we get negative values as well
            float w = x2 - x1;
            float h = y2 - y1;
            // For negative widths/heights, adjust position so shapes align consistently
            // Rects flip/mirror, but ellipses/arcs should be positioned consistently
            if (w < 0) { x += (-w); } // Move right
            if (h < 0) { y += (-h); } // Move down
            x1 = x; y1 = y; x2 = w; y2 = h;
        } else if (mode == PApplet.CENTER) {
            // Find center
            float x = (x2 + x1) / 2f;
            float y = (y2 + y1) / 2f;
            // Calculate width and height
            // Don't use abs(), so we get negative values as well
            float w = x2 - x1;
            float h = y2 - y1;
            x1 = x; y1 = y; x2 = w; y2 = h;
        } else if (mode == PApplet.RADIUS) {
            // Find Center
            float x = (x2 + x1) / 2f;
            float y = (y2 + y1) / 2f;
            // Calculate radii
            // Don't use abs(), so we get negative values as well
            float r1 = (x2 - x1) / 2f;
            float r2 = (y2 - y1) / 2f;
            x1 = x; y1 = y; x2 = r1; y2 = r2;
        }

        if (shape.equals("ellipse")) {
            p.ellipseMode(mode);
            p.ellipse(x1, y1, x2, y2);
        } else if (shape.equals("arc")) {
            // Draw four arcs with gaps inbetween
            final float GAP = PApplet.radians(20);
            p.ellipseMode(mode);
            p.arc(x1, y1, x2, y2, 0 + GAP, PApplet.HALF_PI - GAP);
            p.arc(x1, y1, x2, y2, PApplet.HALF_PI + GAP, PApplet.PI - GAP);
            p.arc(x1, y1, x2, y2, PApplet.PI + GAP, PApplet.PI + PApplet.HALF_PI - GAP);
            p.arc(x1, y1, x2, y2, PApplet.PI + PApplet.HALF_PI + GAP, PApplet.TWO_PI - GAP);
        } else if (shape.equals("rect")) {
            p.rectMode(mode);
            p.rect(x1, y1, x2, y2);
        }
    }

    /**
     * Helper to draw shapes in all four quadrants with various coordinate configurations
     */
    private void drawShapesInQuadrants(PApplet p, String shape, int mode) {
        p.translate(p.width / 2f, p.height / 2f);

        // Quadrant I (Bottom Right)
        //              P1      P2
        shapeCorners(p, shape, mode,  5,  5, 25, 15); // P1 Top Left,     P2 Bottom Right
        shapeCorners(p, shape, mode,  5, 20, 25, 30); // P1 Bottom Left,  P2 Top Right
        shapeCorners(p, shape, mode, 25, 45,  5, 35); // P1 Bottom Right, P2 Top Left
        shapeCorners(p, shape, mode, 25, 50,  5, 60); // P1 Top Right,    P2 Bottom Left

        // Quadrant II (Bottom Left)
        shapeCorners(p, shape, mode, -25,  5,  -5, 15);
        shapeCorners(p, shape, mode, -25, 20,  -5, 30);
        shapeCorners(p, shape, mode,  -5, 45, -25, 35);
        shapeCorners(p, shape, mode,  -5, 50, -25, 60);

        // Quadrant III (Top Left)
        shapeCorners(p, shape, mode, -25, -60,  -5, -50);
        shapeCorners(p, shape, mode, -25, -35,  -5, -45);
        shapeCorners(p, shape, mode,  -5, -20, -25, -30);
        shapeCorners(p, shape, mode,  -5, -15, -25,  -5);

        // Quadrant IV (Top Right)
        shapeCorners(p, shape, mode,  5, -60, 25, -50);
        shapeCorners(p, shape, mode,  5, -35, 25, -45);
        shapeCorners(p, shape, mode, 25, -20,  5, -30);
        shapeCorners(p, shape, mode, 25, -15,  5,  -5);
    }

    private ProcessingSketch createShapeModeTest(String shape, int mode) {
        return new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }

            @Override
            public void draw(PApplet p) {
                drawShapesInQuadrants(p, shape, mode);
            }
        };
    }

    // ========== Ellipse Mode Tests ==========

    @Test
    @Order(1)
    @Tag("ellipse")
    @DisplayName("Ellipse with CORNERS mode")
    public void testEllipseCorners() {
        assertVisualMatch("shape-modes/ellipse-corners",
                createShapeModeTest("ellipse", PApplet.CORNERS),
                new TestConfig(60, 125));
    }

    @Test
    @Order(2)
    @Tag("ellipse")
    @DisplayName("Ellipse with CORNER mode")
    public void testEllipseCorner() {
        assertVisualMatch("shape-modes/ellipse-corner",
                createShapeModeTest("ellipse", PApplet.CORNER),
                new TestConfig(60, 125));
    }

    @Test
    @Order(3)
    @Tag("ellipse")
    @DisplayName("Ellipse with CENTER mode")
    public void testEllipseCenter() {
        assertVisualMatch("shape-modes/ellipse-center",
                createShapeModeTest("ellipse", PApplet.CENTER),
                new TestConfig(60, 125));
    }

    @Test
    @Order(4)
    @Tag("ellipse")
    @DisplayName("Ellipse with RADIUS mode")
    public void testEllipseRadius() {
        assertVisualMatch("shape-modes/ellipse-radius",
                createShapeModeTest("ellipse", PApplet.RADIUS),
                new TestConfig(60, 125));
    }

    // ========== Arc Mode Tests ==========

    @Test
    @Order(5)
    @Tag("arc")
    @DisplayName("Arc with CORNERS mode")
    public void testArcCorners() {
        assertVisualMatch("shape-modes/arc-corners",
                createShapeModeTest("arc", PApplet.CORNERS),
                new TestConfig(60, 125));
    }

    @Test
    @Order(6)
    @Tag("arc")
    @DisplayName("Arc with CORNER mode")
    public void testArcCorner() {
        assertVisualMatch("shape-modes/arc-corner",
                createShapeModeTest("arc", PApplet.CORNER),
                new TestConfig(60, 125));
    }

    @Test
    @Order(7)
    @Tag("arc")
    @DisplayName("Arc with CENTER mode")
    public void testArcCenter() {
        assertVisualMatch("shape-modes/arc-center",
                createShapeModeTest("arc", PApplet.CENTER),
                new TestConfig(60, 125));
    }

    @Test
    @Order(8)
    @Tag("arc")
    @DisplayName("Arc with RADIUS mode")
    public void testArcRadius() {
        assertVisualMatch("shape-modes/arc-radius",
                createShapeModeTest("arc", PApplet.RADIUS),
                new TestConfig(60, 125));
    }

    // ========== Rect Mode Tests ==========

    @Test
    @Order(9)
    @Tag("rect")
    @DisplayName("Rect with CORNERS mode")
    public void testRectCorners() {
        assertVisualMatch("shape-modes/rect-corners",
                createShapeModeTest("rect", PApplet.CORNERS),
                new TestConfig(60, 125));
    }

    @Test
    @Order(10)
    @Tag("rect")
    @DisplayName("Rect with CORNER mode")
    public void testRectCorner() {
        assertVisualMatch("shape-modes/rect-corner",
                createShapeModeTest("rect", PApplet.CORNER),
                new TestConfig(60, 125));
    }

    @Test
    @Order(11)
    @Tag("rect")
    @DisplayName("Rect with CENTER mode")
    public void testRectCenter() {
        assertVisualMatch("shape-modes/rect-center",
                createShapeModeTest("rect", PApplet.CENTER),
                new TestConfig(60, 125));
    }

    @Test
    @Order(12)
    @Tag("rect")
    @DisplayName("Rect with RADIUS mode")
    public void testRectRadius() {
        assertVisualMatch("shape-modes/rect-radius",
                createShapeModeTest("rect", PApplet.RADIUS),
                new TestConfig(60, 125));
    }

    // ========== Negative Dimensions Tests ==========

    @Test
    @Order(13)
    @Tag("negative-dimensions")
    @DisplayName("Rect with negative dimensions")
    public void testRectNegativeDimensions() {
        assertVisualMatch("shape-modes/rect-negative-dimensions", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }

            @Override
            public void draw(PApplet p) {
                p.translate(p.width / 2f, p.height / 2f);
                p.rectMode(PApplet.CORNER);
                p.rect(0, 0,  20,  10);
                p.fill(255, 0, 0);
                p.rect(0, 0, -20,  10);
                p.fill(0, 255, 0);
                p.rect(0, 0,  20, -10);
                p.fill(0, 0, 255);
                p.rect(0, 0, -20, -10);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(14)
    @Tag("negative-dimensions")
    @DisplayName("Ellipse with negative dimensions")
    public void testEllipseNegativeDimensions() {
        assertVisualMatch("shape-modes/ellipse-negative-dimensions", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }

            @Override
            public void draw(PApplet p) {
                p.translate(p.width / 2f, p.height / 2f);
                p.ellipseMode(PApplet.CORNER);
                p.ellipse(0, 0,  20,  10);
                p.fill(255, 0, 0);
                p.ellipse(0, 0, -20,  10);
                p.fill(0, 255, 0);
                p.ellipse(0, 0,  20, -10);
                p.fill(0, 0, 255);
                p.ellipse(0, 0, -20, -10);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(15)
    @Tag("negative-dimensions")
    @DisplayName("Arc with negative dimensions")
    public void testArcNegativeDimensions() {
        assertVisualMatch("shape-modes/arc-negative-dimensions", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(200);
                p.fill(255);
                p.stroke(0);
            }

            @Override
            public void draw(PApplet p) {
                p.translate(p.width / 2f, p.height / 2f);
                p.ellipseMode(PApplet.CORNER);
                p.arc(0, 0,  20,  10, 0, PApplet.PI + PApplet.HALF_PI);
                p.fill(255, 0, 0);
                p.arc(0, 0, -20,  10, 0, PApplet.PI + PApplet.HALF_PI);
                p.fill(0, 255, 0);
                p.arc(0, 0,  20, -10, 0, PApplet.PI + PApplet.HALF_PI);
                p.fill(0, 0, 255);
                p.arc(0, 0, -20, -10, 0, PApplet.PI + PApplet.HALF_PI);
            }
        }, new TestConfig(50, 50));
    }
}