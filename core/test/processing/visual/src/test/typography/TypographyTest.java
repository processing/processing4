package processing.visual.src.test.typography;

import org.junit.jupiter.api.*;
import processing.core.*;
import processing.visual.src.test.base.VisualTest;
import processing.visual.src.core.ProcessingSketch;
import processing.visual.src.core.TestConfig;

@Tag("typography")
@Tag("text")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TypographyTest extends VisualTest {

    @Nested
    @Tag("font")
    @DisplayName("textFont Tests")
    class TextFontTests {

        @Test
        @Order(1)
        @DisplayName("Default font rendering")
        public void testDefaultFont() {
            assertVisualMatch("typography/font/default-font", new ProcessingSketch() {
                @Override
                public void setup(PApplet p) {
                    PFont font = p.createFont("SansSerif", 20);
                    p.textFont(font);
                    p.textSize(20);
                    p.textAlign(PApplet.LEFT, PApplet.BASELINE);
                }

                @Override
                public void draw(PApplet p) {
                    p.background(255);
                    p.fill(0);  // ← Must be in draw(), not just setup()
                    p.text("test", 5, 25);  // ← Move away from edge
                }
            }, new TestConfig(50, 50));
        }

        @Test
        @Order(2)
        @DisplayName("Monospace font rendering")
        public void testMonospaceFont() {
            assertVisualMatch("typography/font/monospace-font", new ProcessingSketch() {
                @Override
                public void setup(PApplet p) {
                    PFont mono = p.createFont("Monospaced", 20);
                    p.textFont(mono);
                    p.textAlign(PApplet.LEFT, PApplet.BASELINE);
                }

                @Override
                public void draw(PApplet p) {
                    p.background(255);
                    p.fill(0);  // ← Add this
                    p.text("test", 5, 25);
                }
            }, new TestConfig(50, 50));
        }

        @Test
        @Order(3)
        @DisplayName("System font rendering")
        public void testSystemFont() {
            assertVisualMatch("typography/font/system-font", new ProcessingSketch() {
                @Override
                public void setup(PApplet p) {
                    PFont font = p.createFont("Serif", 32);
                    p.textFont(font);
                }

                @Override
                public void draw(PApplet p) {
                    p.background(255);
                    p.fill(0);  // ← Add this
                    p.text("test", 10, 50);  // ← Better positioning
                }
            }, new TestConfig(100, 100));
        }
    }


    @Nested
    @Tag("alignment")
    @DisplayName("textAlign Tests")
    class TextAlignTests {

        @Test
        @DisplayName("All horizontal and vertical alignments with single word")
        public void testAllAlignmentsSingleWord() {
            int[][] alignments = {
                    {PApplet.LEFT, PApplet.TOP},
                    {PApplet.CENTER, PApplet.TOP},
                    {PApplet.RIGHT, PApplet.TOP},
                    {PApplet.LEFT, PApplet.CENTER},
                    {PApplet.CENTER, PApplet.CENTER},
                    {PApplet.RIGHT, PApplet.CENTER},
                    {PApplet.LEFT, PApplet.BOTTOM},
                    {PApplet.CENTER, PApplet.BOTTOM},
                    {PApplet.RIGHT, PApplet.BOTTOM}
            };

            TestConfig config = new TestConfig(300, 300);

            for (int[] alignment : alignments) {
                final int alignX = alignment[0];
                final int alignY = alignment[1];
                final String alignName = getAlignmentName(alignX, alignY);

                assertVisualMatch("typography/align/single-word-" + alignName,
                        new ProcessingSketch() {
                            PFont font;

                            @Override
                            public void setup(PApplet p) {
                                font = p.createFont("SansSerif", 60);
                                p.textFont(font);
                            }

                            @Override
                            public void draw(PApplet p) {
                                p.background(255);
                                p.textAlign(alignX, alignY);
                                p.fill(0);  // ← Must be in draw()
                                p.text("Single Line", p.width / 2, p.height / 2);

                                // Draw bounding box
                                p.noFill();
                                p.stroke(255, 0, 0);
                                p.strokeWeight(2);

                                float tw = p.textWidth("Single Line");
                                float th = p.textAscent() + p.textDescent();
                                float x = calculateX(p, alignX, p.width / 2f, tw);
                                float y = calculateY(p, alignY, p.height / 2f, th);
                                p.rect(x, y, tw, th);
                            }
                        }, config);
            }
        }

        @Test
        @DisplayName("Multi-line text with manual line breaks")
        public void testMultiLineManualText() {
            int[][] alignments = {
                    {PApplet.LEFT, PApplet.TOP},
                    {PApplet.CENTER, PApplet.TOP},
                    {PApplet.RIGHT, PApplet.TOP},
                    {PApplet.LEFT, PApplet.CENTER},
                    {PApplet.CENTER, PApplet.CENTER},
                    {PApplet.RIGHT, PApplet.CENTER},
                    {PApplet.LEFT, PApplet.BOTTOM},
                    {PApplet.CENTER, PApplet.BOTTOM},
                    {PApplet.RIGHT, PApplet.BOTTOM}
            };

            TestConfig config = new TestConfig(150, 100);

            for (int[] alignment : alignments) {
                final int alignX = alignment[0];
                final int alignY = alignment[1];
                final String alignName = getAlignmentName(alignX, alignY);

                assertVisualMatch("typography/align/multi-line-" + alignName,
                        new ProcessingSketch() {
                            PFont font;

                            @Override
                            public void setup(PApplet p) {
                                font = p.createFont("SansSerif", 12);
                                p.textFont(font);
                            }

                            @Override
                            public void draw(PApplet p) {
                                p.background(255);

                                float xPos = 20;
                                float yPos = 20;
                                float boxWidth = 100;
                                float boxHeight = 60;

                                // Draw box
                                p.noFill();
                                p.stroke(200);
                                p.strokeWeight(2);
                                p.rect(xPos, yPos, boxWidth, boxHeight);

                                // Draw text
                                p.fill(0);  // ← Must be before text
                                p.noStroke();
                                p.textAlign(alignX, alignY);
                                p.text("Line 1\nLine 2\nLine 3", xPos, yPos, boxWidth, boxHeight);

                                // Draw bounding box (optional)
                                p.noFill();
                                p.stroke(255, 0, 0);
                                p.strokeWeight(1);
                            }
                        }, config);
            }
        }

        // Helper methods
        private String getAlignmentName(int alignX, int alignY) {
            String x = alignX == PApplet.LEFT ? "left" :
                    alignX == PApplet.CENTER ? "center" : "right";
            String y = alignY == PApplet.TOP ? "top" :
                    alignY == PApplet.CENTER ? "center" : "bottom";
            return x + "-" + y;
        }

        private float calculateX(PApplet p, int alignX, float x, float tw) {
            if (alignX == PApplet.LEFT) return x;
            if (alignX == PApplet.CENTER) return x - tw / 2;
            return x - tw;
        }

        private float calculateY(PApplet p, int alignY, float y, float th) {
            if (alignY == PApplet.TOP) return y;
            if (alignY == PApplet.CENTER) return y - th / 2;
            return y - th;
        }
    }


    @Nested
    @Tag("size")
    @DisplayName("textSize Tests")
    class TextSizeTests {

        @Test
        @DisplayName("Text sizes comparison")
        public void testTextSizes() {
            assertVisualMatch("typography/size/sizes-comparison", new ProcessingSketch() {
                PFont font;

                @Override
                public void setup(PApplet p) {
                    font = p.createFont("SansSerif", 12);
                    p.textFont(font);
                    p.textAlign(PApplet.LEFT, PApplet.BASELINE);
                }

                @Override
                public void draw(PApplet p) {
                    p.background(255);
                    p.fill(0);  // ← Add this

                    int[] sizes = {12, 16, 20, 24, 30};
                    float yOffset = 20;

                    for (int size : sizes) {
                        p.textSize(size);
                        p.text("Size: " + size + "px", 10, yOffset);
                        yOffset += size + 5;
                    }
                }
            }, new TestConfig(300, 200));
        }
    }

    @Nested
    @Tag("leading")
    @DisplayName("textLeading Tests")
    class TextLeadingTests {

        @Test
        @DisplayName("Text leading with different values")
        public void testTextLeading() {
            assertVisualMatch("typography/leading/different-values", new ProcessingSketch() {
                PFont font;

                @Override
                public void setup(PApplet p) {
                    font = p.createFont("SansSerif", 16);
                    p.textFont(font);
                    p.textSize(16);
                    p.textAlign(PApplet.LEFT, PApplet.BASELINE);
                }

                @Override
                public void draw(PApplet p) {
                    p.background(255);
                    p.fill(0);  // ← Add this

                    int[] leadingValues = {10, 20, 30};
                    float yOffset = 25;

                    for (int leading : leadingValues) {
                        p.textLeading(leading);
                        p.text("Leading: " + leading, 10, yOffset);
                        yOffset += 25;
                        p.text("Line 1\nLine 2", 10, yOffset);
                        yOffset += leading * 2 + 15;
                    }
                }
            }, new TestConfig(300, 250));
        }
    }


    @Nested
    @Tag("width")
    @DisplayName("textWidth Tests")
    class TextWidthTests {

        @Test
        @DisplayName("Verify width of a string")
        public void testTextWidth() {
            assertVisualMatch("typography/width/string-width", new ProcessingSketch() {
                @Override
                public void setup(PApplet p) {
                    p.textSize(20);
                }

                @Override
                public void draw(PApplet p) {
                    p.background(255);

                    String text = "Width Test";
                    float width = p.textWidth(text);

                    p.fill(0);
                    p.text(text, 0, 30);

                    p.noFill();
                    p.stroke(255, 0, 0);
                    p.rect(0, 10, width, 20);
                }
            }, new TestConfig(100, 100));
        }
    }
}