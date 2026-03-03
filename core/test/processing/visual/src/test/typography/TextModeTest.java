package processing.visual.src.test.typography;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("typography")
@Tag("textmode")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TextModeTest extends VisualTest {

    @Test
    @Order(1)
    @DisplayName("Text in MODEL mode")
    public void testTextModelMode() {
        assertVisualMatch("typography/textmode/model-mode", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.textSize(20);
                p.textMode(PApplet.MODEL);
                p.textAlign(PApplet.LEFT, PApplet.BASELINE);
            }

            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.fill(0);
                p.text("MODEL mode", 10, 30);
            }
        }, new TestConfig(150, 50));
    }

    @Test
    @Order(2)
    @DisplayName("Text in SHAPE mode")
    public void testTextShapeMode() {
        assertVisualMatch("typography/textmode/shape-mode", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.textSize(20);
                p.textMode(PApplet.SHAPE);
                p.textAlign(PApplet.LEFT, PApplet.BASELINE);
            }

            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.fill(0);
                p.text("SHAPE mode", 10, 30);
            }
        }, new TestConfig(150, 50));
    }

    @Test
    @Order(3)
    @DisplayName("Text mode switch from MODEL to SHAPE")
    public void testTextModeSwitching() {
        assertVisualMatch("typography/textmode/mode-switching", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.textSize(16);
                p.textAlign(PApplet.LEFT, PApplet.BASELINE);
            }

            @Override
            public void draw(PApplet p) {
                p.background(255);
                p.fill(0);

                p.textMode(PApplet.MODEL);
                p.text("MODEL", 10, 25);

                p.textMode(PApplet.SHAPE);
                p.text("SHAPE", 10, 50);
            }
        }, new TestConfig(150, 70));
    }
}