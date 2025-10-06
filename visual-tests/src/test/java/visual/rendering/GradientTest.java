package visual.rendering;

import org.junit.jupiter.api.*;
import processing.core.*;
import visual.base.VisualTest;
import processing.test.visual.ProcessingSketch;
import processing.test.visual.TestConfig;

@Tag("rendering")
@Tag("gradients")
public class GradientTest extends VisualTest {

    @Test
    @DisplayName("Linear gradient renders correctly")
    public void testLinearGradient() {
        TestConfig config = new TestConfig(600, 400);

        assertVisualMatch("rendering/linear-gradient", new ProcessingSketch() {
            @Override
            public void setup(PApplet p) {
                p.noStroke();
            }

            @Override
            public void draw(PApplet p) {
                for (int y = 0; y < p.height; y++) {
                    float inter = PApplet.map(y, 0, p.height, 0, 1);
                    int c = p.lerpColor(p.color(255, 0, 0), p.color(0, 0, 255), inter);
                    p.stroke(c);
                    p.line(0, y, p.width, y);
                }
            }
        }, config);
    }
}