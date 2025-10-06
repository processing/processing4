//// SimpleTest.java - Fixed version for quick verification
//import processing.core.*;
//import java.util.*;
//
//public class SimpleTest {
//
//    public static void main(String[] args) {
//        System.out.println("=== Processing Visual Testing - Quick Test ===\n");
//
//        try {
//            PApplet tempApplet = new PApplet();
//
//            ImageComparator comparator = new ImageComparator(tempApplet);
//            VisualTestRunner tester = new VisualTestRunner(comparator);
//
//            ProcessingSketch redCircle = new SimpleRedCircle();
//            TestResult result1 = tester.runVisualTest("red-circle", redCircle);
//            result1.printResult();
//
//            ProcessingSketch blueSquare = new SimpleBlueSquare();
//            TestResult result2 = tester.runVisualTest("blue-square", blueSquare);
//            result2.printResult();
//
//            // Step 4: Test comparison with identical image (should pass on second run)
//            if (!result1.isFirstRun) {
//                TestResult result3 = tester.runVisualTest("red-circle", redCircle);
//                result3.printResult();
//
//                if (result3.passed) {
//                    System.out.println("✓ Identical image comparison works!");
//                } else {
//                    System.out.println("✗ Identical image comparison failed!");
//                }
//            }
//            ProcessingTestSuite suite = new ProcessingTestSuite(tester);
//            suite.addTest("suite-red", new SimpleRedCircle());
//            suite.addTest("suite-blue", new SimpleBlueSquare());
//            suite.addTest("suite-gradient", new SimpleGradient());
//
//            List<TestResult> suiteResults = suite.runAll();
//
//            long passed = suiteResults.stream().filter(r -> r.passed).count();
//            long total = suiteResults.size();
//            System.out.println("Suite results: " + passed + "/" + total + " passed");
//            System.exit(0);
//
//        } catch (Exception e) {
//            System.err.println("Test failed with error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // Simple test sketches
//    static class SimpleRedCircle implements ProcessingSketch {
//        public void setup(PApplet p) {
//            p.noStroke();
//        }
//
//        public void draw(PApplet p) {
//            p.background(255, 255, 255);
//            p.fill(255, 0, 0);
//            p.ellipse(p.width/2, p.height/2, 100, 100);
//        }
//    }
//
//    static class SimpleBlueSquare implements ProcessingSketch {
//        public void setup(PApplet p) {
//            p.stroke(0);
//            p.strokeWeight(2);
//        }
//
//        public void draw(PApplet p) {
//            p.background(255);
//            p.fill(0, 0, 255);
//            p.rect(p.width/2 - 50, p.height/2 - 50, 100, 100);
//        }
//    }
//
//    static class SimpleGreenCircle implements ProcessingSketch {
//        public void setup(PApplet p) {
//            p.noStroke();
//        }
//
//        public void draw(PApplet p) {
//            p.background(255, 255, 255);
//            p.fill(0, 255, 0); // Green instead of red
//            p.ellipse(p.width/2, p.height/2, 100, 100);
//        }
//    }
//
//    static class SimpleGradient implements ProcessingSketch {
//        public void setup(PApplet p) {
//            p.noStroke();
//        }
//
//        public void draw(PApplet p) {
//            for (int y = 0; y < p.height; y++) {
//                float inter = PApplet.map(y, 0, p.height, 0, 1);
//                int c = p.lerpColor(p.color(255, 0, 0), p.color(0, 0, 255), inter);
//                p.stroke(c);
//                p.line(0, y, p.width, y);
//            }
//        }
//    }
//}