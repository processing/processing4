package processing.visual.src.test.blendmodes;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("blend-modes")
@Tag("rendering")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlendModeTest extends VisualTest {

    // Draws base rects with BLEND, then overlays a green rect using the given mode
    private ProcessingSketch createBlendTest(int mode) {
        return new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(128);
                p.noStroke();
            }

            @Override
            public void draw(PApplet p) {
                // Base layer — always drawn with normal BLEND
                p.blendMode(PApplet.BLEND);
                p.fill(200, 60, 60);
                p.rect(5, 5, 30, 30);
                p.fill(60, 60, 200);
                p.rect(15, 15, 30, 30);

                p.blendMode(mode);
                p.fill(60, 200, 60, 160);
                p.rect(10, 10, 30, 30);
            }
        };
    }

    // ========== Individual Blend Modes ==========

    @Test
    @Order(1)
    @DisplayName("blendMode(BLEND)")
    public void testBlend() {
        assertVisualMatch("blend-modes/blend",
                createBlendTest(PApplet.BLEND),
                new TestConfig(50, 50));
    }

    @Test
    @Order(2)
    @DisplayName("blendMode(ADD)")
    public void testAdd() {
        assertVisualMatch("blend-modes/add",
                createBlendTest(PApplet.ADD),
                new TestConfig(50, 50));
    }

    @Test
    @Order(3)
    @DisplayName("blendMode(SUBTRACT)")
    public void testSubtract() {
        assertVisualMatch("blend-modes/subtract",
                createBlendTest(PApplet.SUBTRACT),
                new TestConfig(50, 50));
    }

    @Test
    @Order(4)
    @DisplayName("blendMode(MULTIPLY)")
    public void testMultiply() {
        assertVisualMatch("blend-modes/multiply",
                createBlendTest(PApplet.MULTIPLY),
                new TestConfig(50, 50));
    }

    @Test
    @Order(5)
    @DisplayName("blendMode(SCREEN)")
    public void testScreen() {
        assertVisualMatch("blend-modes/screen",
                createBlendTest(PApplet.SCREEN),
                new TestConfig(50, 50));
    }

    @Test
    @Order(6)
    @DisplayName("blendMode(DARKEST)")
    public void testDarkest() {
        assertVisualMatch("blend-modes/darkest",
                createBlendTest(PApplet.DARKEST),
                new TestConfig(50, 50));
    }

    @Test
    @Order(7)
    @DisplayName("blendMode(LIGHTEST)")
    public void testLightest() {
        assertVisualMatch("blend-modes/lightest",
                createBlendTest(PApplet.LIGHTEST),
                new TestConfig(50, 50));
    }

    @Test
    @Order(8)
    @DisplayName("blendMode(DIFFERENCE)")
    public void testDifference() {
        assertVisualMatch("blend-modes/difference",
                createBlendTest(PApplet.DIFFERENCE),
                new TestConfig(50, 50));
    }

    @Test
    @Order(9)
    @DisplayName("blendMode(EXCLUSION)")
    public void testExclusion() {
        assertVisualMatch("blend-modes/exclusion",
                createBlendTest(PApplet.EXCLUSION),
                new TestConfig(50, 50));
    }

    @Test
    @Order(10)
    @DisplayName("blendMode(REPLACE)")
    public void testReplace() {
        assertVisualMatch("blend-modes/replace",
                createBlendTest(PApplet.REPLACE),
                new TestConfig(50, 50));
    }

    // ========== Sequential Mode Switch ==========

    @Test
    @Order(11)
    @DisplayName("Switching blend modes mid-sketch")
    public void testModeSwitch() {
        assertVisualMatch("blend-modes/mode-switch", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.background(128);
                p.noStroke();
            }

            @Override
            public void draw(PApplet p) {
                // Each strip should only reflect its own blend mode
                p.blendMode(PApplet.ADD);
                p.fill(200, 60, 60, 160);
                p.rect(5, 5, 20, 40);

                p.blendMode(PApplet.MULTIPLY);
                p.fill(60, 200, 60, 160);
                p.rect(15, 5, 20, 40);

                p.blendMode(PApplet.BLEND);
                p.fill(60, 60, 200, 160);
                p.rect(25, 5, 20, 40);
            }
        }, new TestConfig(50, 50));
    }
    @Test
    @Order(12)
    @DisplayName("blendMode(OVERLAY)")
    public void testOverlay() {
        assertVisualMatch("blend-modes/overlay",
                createBlendTest(PApplet.OVERLAY),
                new TestConfig(50, 50));
    }

    @Test
    @Order(13)
    @DisplayName("blendMode(HARD_LIGHT)")
    public void testHardLight() {
        assertVisualMatch("blend-modes/hard-light",
                createBlendTest(PApplet.HARD_LIGHT),
                new TestConfig(50, 50));
    }

    @Test
    @Order(14)
    @DisplayName("blendMode(SOFT_LIGHT)")
    public void testSoftLight() {
        assertVisualMatch("blend-modes/soft-light",
                createBlendTest(PApplet.SOFT_LIGHT),
                new TestConfig(50, 50));
    }
}
