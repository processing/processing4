package processing.visual.src.test.shapes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("shapes")
@Tag("stroke")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StrokeTest extends VisualTest {

    @Test
    @Order(1)
    @DisplayName("Stroke weight variations")
    public void testStrokeWeight() {
        assertVisualMatch("shapes/stroke-weight", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.noFill();
            }
            @Override
            public void draw(PApplet p) {
                int[] weights = {1, 2, 4, 6, 8};
                for (int i = 0; i < weights.length; i++) {
                    p.strokeWeight(weights[i]);
                    p.line(5, 8 + i * 9, 45, 8 + i * 9);
                }
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @DisplayName("Stroke cap ROUND")
    public void testStrokeCapRound() {
        assertVisualMatch("shapes/stroke-cap-round", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.strokeWeight(8);
                p.strokeCap(PApplet.ROUND);
            }
            @Override
            public void draw(PApplet p) {
                p.line(10, 15, 40, 15);
                p.line(10, 30, 40, 30);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(3)
    @DisplayName("Stroke cap SQUARE")
    public void testStrokeCapSquare() {
        assertVisualMatch("shapes/stroke-cap-square", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.strokeWeight(8);
                p.strokeCap(PApplet.SQUARE);
            }
            @Override
            public void draw(PApplet p) {
                p.line(10, 15, 40, 15);
                p.line(10, 30, 40, 30);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(4)
    @DisplayName("Stroke join MITER")
    public void testStrokeJoinMiter() {
        assertVisualMatch("shapes/stroke-join-miter", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.strokeWeight(4);
                p.noFill();
                p.strokeJoin(PApplet.MITER);
            }
            @Override
            public void draw(PApplet p) {
                p.rect(10, 10, 30, 30);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(5)
    @DisplayName("Stroke join ROUND")
    public void testStrokeJoinRound() {
        assertVisualMatch("shapes/stroke-join-round", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.strokeWeight(4);
                p.noFill();
                p.strokeJoin(PApplet.ROUND);
            }
            @Override
            public void draw(PApplet p) {
                p.rect(10, 10, 30, 30);
            }
        }, new TestConfig(50, 50));
    }

    @Test
    @Order(6)
    @DisplayName("Stroke join BEVEL")
    public void testStrokeJoinBevel() {
        assertVisualMatch("shapes/stroke-join-bevel", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(255);
                p.strokeWeight(4);
                p.noFill();
                p.strokeJoin(PApplet.BEVEL);
            }
            @Override
            public void draw(PApplet p) {
                p.rect(10, 10, 30, 30);
            }
        }, new TestConfig(50, 50));
    }
}