// Processing Visual Test Runner - Modular test execution infrastructure
// Uses ImageComparator for sophisticated pixel matching

import processing.core.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

// Core visual tester class
public class VisualTestRunner {

    private String screenshotDir;
    private PixelMatchingAlgorithm pixelMatcher;
    private String platform;

    public VisualTestRunner(PixelMatchingAlgorithm pixelMatcher) {
        this.pixelMatcher = pixelMatcher;
        this.screenshotDir = "__screenshots__";
        this.platform = detectPlatform();
        createDirectoryIfNotExists(screenshotDir);
    }

    public VisualTestRunner(PixelMatchingAlgorithm pixelMatcher, String screenshotDir) {
        this.pixelMatcher = pixelMatcher;
        this.screenshotDir = screenshotDir;
        this.platform = detectPlatform();
        createDirectoryIfNotExists(screenshotDir);
    }

    // Main test execution method
    public TestResult runVisualTest(String testName, ProcessingSketch sketch) {
        return runVisualTest(testName, sketch, new TestConfig());
    }

    public TestResult runVisualTest(String testName, ProcessingSketch sketch, TestConfig config) {
        try {
            System.out.println("Running visual test: " + testName);

            // Capture screenshot from sketch
            PImage actualImage = captureSketch(sketch, config);

            // Compare with baseline
            ComparisonResult comparison = compareWithBaseline(testName, actualImage, config);

            return new TestResult(testName, comparison);

        } catch (Exception e) {
            return TestResult.createError(testName, e.getMessage());
        }
    }

    // Capture PImage from Processing sketch
    private PImage captureSketch(ProcessingSketch sketch, TestConfig config) {
        SketchRunner runner = new SketchRunner(sketch, config);
        runner.run();
        return runner.getImage();
    }

    // Compare actual image with baseline
    private ComparisonResult compareWithBaseline(String testName, PImage actualImage, TestConfig config) {
        String baselinePath = getBaselinePath(testName);

        PImage baselineImage = loadBaseline(baselinePath);

        if (baselineImage == null) {
            // First run - save as baseline
            saveBaseline(testName, actualImage);
            return ComparisonResult.createFirstRun();
        }

        // Use your sophisticated pixel matching algorithm
        ComparisonResult result = pixelMatcher.compare(baselineImage, actualImage, config.threshold);

        // Save diff images if test failed
        if (!result.passed && result.diffImage != null) {
            saveDiffImage(testName, result.diffImage);
        }

        return result;
    }

    // Save diff image for debugging
    private void saveDiffImage(String testName, PImage diffImage) {
        String sanitizedName = testName.replaceAll("[^a-zA-Z0-9-_]", "-");
        String diffPath = "diff_" + sanitizedName + "-" + platform + ".png";
        diffImage.save(diffPath);
        System.out.println("Diff image saved: " + diffPath);
    }

    // Utility methods
    private String detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) return "darwin";
        if (os.contains("win")) return "win32";
        return "linux";
    }

    private void createDirectoryIfNotExists(String dir) {
        try {
            Files.createDirectories(Paths.get(dir));
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + dir);
        }
    }

    private String getBaselinePath(String testName) {
        String sanitizedName = testName.replaceAll("[^a-zA-Z0-9-_]", "-");
        return screenshotDir + "/" + sanitizedName + "-" + platform + ".png";
    }

    private PImage loadBaseline(String path) {
        File file = new File(path);
        if (!file.exists()) return null;

        // Create a temporary PApplet to load the image
        PApplet tempApplet = new PApplet();
        return tempApplet.loadImage(path);
    }

    private void saveBaseline(String testName, PImage image) {
        String path = getBaselinePath(testName);
        image.save(path);
        System.out.println("Baseline created: " + path);
    }
}

// Test runner that executes Processing sketches
class SketchRunner extends PApplet {

    private ProcessingSketch userSketch;
    private TestConfig config;
    private PImage capturedImage;
    private boolean rendered = false;

    public SketchRunner(ProcessingSketch userSketch, TestConfig config) {
        this.userSketch = userSketch;
        this.config = config;
    }

    public void settings() {
        size(config.width, config.height);
    }

    public void setup() {
        // Disable animations for consistent testing
        noLoop();

        // Set background if specified
        if (config.backgroundColor != null) {
            background(config.backgroundColor[0], config.backgroundColor[1], config.backgroundColor[2]);
        }

        // Call user setup
        userSketch.setup(this);
    }

    public void draw() {
        if (!rendered) {
            // Call user draw function
            userSketch.draw(this);

            // Capture the frame
            capturedImage = get(); // get() returns a PImage of the entire canvas
            rendered = true;
        }
    }

    public void run() {
        String[] args = {"SketchRunner"};
        PApplet.runSketch(args, this);

        // Wait for rendering to complete
        while (!rendered) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Additional wait time for any processing
        try {
            Thread.sleep(config.renderWaitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Clean up
        exit();
    }

    public PImage getImage() {
        return capturedImage;
    }
}

// Interface for user sketches
interface ProcessingSketch {
    void setup(PApplet p);
    void draw(PApplet p);
}

// Test configuration class
class TestConfig {
    public int width = 800;
    public int height = 600;
    public int[] backgroundColor = {255, 255, 255}; // RGB
    public long renderWaitTime = 2000; // milliseconds
    public double threshold = 0.1;

    public TestConfig() {}

    public TestConfig(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public TestConfig(int width, int height, int[] backgroundColor) {
        this.width = width;
        this.height = height;
        this.backgroundColor = backgroundColor;
    }

    public TestConfig setThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public TestConfig setRenderWaitTime(long waitTime) {
        this.renderWaitTime = waitTime;
        return this;
    }
}

// Enhanced test result with detailed information
class TestResult {
    public String testName;
    public boolean passed;
    public double mismatchRatio;
    public String error;
    public boolean isFirstRun;
    public ComparisonDetails details;

    public TestResult(String testName, ComparisonResult comparison) {
        this.testName = testName;
        this.passed = comparison.passed;
        this.mismatchRatio = comparison.mismatchRatio;
        this.isFirstRun = comparison.isFirstRun;
        this.details = comparison.details;
    }

    public static TestResult createError(String testName, String error) {
        TestResult result = new TestResult();
        result.testName = testName;
        result.passed = false;
        result.error = error;
        return result;
    }

    private TestResult() {} // For error constructor

    public void printResult() {
        System.out.print(testName + ": ");
        if (error != null) {
            System.out.println("ERROR - " + error);
        } else if (isFirstRun) {
            System.out.println("BASELINE CREATED");
        } else if (passed) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED (mismatch: " + String.format("%.4f", mismatchRatio * 100) + "%)");
            if (details != null) {
                details.printDetails();
            }
        }
    }
}

// Test suite for organizing multiple tests
class ProcessingTestSuite {
    private VisualTestRunner tester;
    private List<VisualTest> tests;

    public ProcessingTestSuite(VisualTestRunner tester) {
        this.tester = tester;
        this.tests = new ArrayList<>();
    }

    public void addTest(String name, ProcessingSketch sketch) {
        addTest(name, sketch, new TestConfig());
    }

    public void addTest(String name, ProcessingSketch sketch, TestConfig config) {
        tests.add(new VisualTest(name, sketch, config));
    }

    public List<TestResult> runAll() {
        System.out.println("Running " + tests.size() + " visual tests...");
        List<TestResult> results = new ArrayList<>();

        for (VisualTest test : tests) {
            TestResult result = tester.runVisualTest(test.name, test.sketch, test.config);
            result.printResult();
            results.add(result);
        }

        return results;
    }

    public TestResult runTest(String testName) {
        VisualTest test = tests.stream()
            .filter(t -> t.name.equals(testName))
        .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Test not found: " + testName));

        return tester.runVisualTest(test.name, test.sketch, test.config);
    }

    public List<String> getTestNames() {
        return tests.stream().map(t -> t.name).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    // Helper class for internal test storage
    private static class VisualTest {
        String name;
        ProcessingSketch sketch;
        TestConfig config;

        VisualTest(String name, ProcessingSketch sketch, TestConfig config) {
            this.name = name;
            this.sketch = sketch;
            this.config = config;
        }
    }
}

// Baseline manager for updating reference images
class BaselineManager {
    private VisualTestRunner tester;

    public BaselineManager(VisualTestRunner tester) {
        this.tester = tester;
    }

    public void updateBaseline(String testName, ProcessingSketch sketch) {
        updateBaseline(testName, sketch, new TestConfig());
    }

    public void updateBaseline(String testName, ProcessingSketch sketch, TestConfig config) {
        System.out.println("Updating baseline for: " + testName);

        // Capture new image
        SketchRunner runner = new SketchRunner(sketch, config);
        runner.run();
        PImage newImage = runner.getImage();

        // Save as baseline
        String baselinePath = "__screenshots__/" +
        testName.replaceAll("[^a-zA-Z0-9-_]", "-") +
                "-" + detectPlatform() + ".png";
        newImage.save(baselinePath);

        System.out.println("Baseline updated: " + baselinePath);
    }

    public void updateAllBaselines(ProcessingTestSuite suite) {
        System.out.println("Updating all baselines...");
        List<String> testNames = suite.getTestNames();

        for (String testName : testNames) {
            // Re-run the test to get the sketch and config
            TestResult result = suite.runTest(testName);
            // Note: In a real implementation, you'd need to store the sketch reference
            // This is a simplified version
        }
    }

    private String detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) return "darwin";
        if (os.contains("win")) return "win32";
        return "linux";
    }
}

// Test execution utilities
class TestExecutor {

    public static void runSingleTest(String testName, ProcessingSketch sketch) {
        runSingleTest(testName, sketch, new TestConfig());
    }

    public static void runSingleTest(String testName, ProcessingSketch sketch, TestConfig config) {
        // Initialize comparator
        PApplet tempApplet = new PApplet();
        ImageComparator comparator = new ImageComparator(tempApplet);

        // Run test
        VisualTestRunner tester = new VisualTestRunner(comparator);
        TestResult result = tester.runVisualTest(testName, sketch, config);
        result.printResult();
    }

    public static void updateSingleBaseline(String testName, ProcessingSketch sketch) {
        updateSingleBaseline(testName, sketch, new TestConfig());
    }

    public static void updateSingleBaseline(String testName, ProcessingSketch sketch, TestConfig config) {
        // Initialize comparator
        PApplet tempApplet = new PApplet();
        ImageComparator comparator = new ImageComparator(tempApplet);

        // Update baseline
        VisualTestRunner tester = new VisualTestRunner(comparator);
        BaselineManager manager = new BaselineManager(tester);
        manager.updateBaseline(testName, sketch, config);
    }
}

// Example usage and test implementations
class ProcessingVisualTestExamples {

    public static void main(String[] args) {
        // Initialize with your sophisticated pixel matching algorithm
        PApplet tempApplet = new PApplet();

        ImageComparator comparator = new ImageComparator(tempApplet);
        VisualTestRunner tester = new VisualTestRunner(comparator);
        ProcessingTestSuite suite = new ProcessingTestSuite(tester);

        // Add example tests
        suite.addTest("red-circle", new RedCircleSketch());
        suite.addTest("blue-square", new BlueSquareSketch());
        suite.addTest("gradient-background", new GradientBackgroundSketch(),
            new TestConfig(600, 400));
        suite.addTest("complex-pattern", new ComplexPatternSketch(),
            new TestConfig(800, 600).setThreshold(0.15));

        // Run all tests
        List<TestResult> results = suite.runAll();

        // Print detailed summary
        printTestSummary(results);

        // Handle command line arguments
        if (args.length > 0) {
            handleCommandLineArgs(args, suite);
        }
    }

    private static void printTestSummary(List<TestResult> results) {
        long passed = results.stream().filter(r -> r.passed).count();
        long failed = results.size() - passed;
        long baselines = results.stream().filter(r -> r.isFirstRun).count();
        long errors = results.stream().filter(r -> r.error != null).count();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total: " + results.size());
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Baselines Created: " + baselines);
        System.out.println("Errors: " + errors);

        // Print detailed failure information
        results.stream()
            .filter(r -> !r.passed && !r.isFirstRun && r.error == null)
        .forEach(r -> {
            System.out.println("\n--- Failed Test: " + r.testName + " ---");
            if (r.details != null) {
                r.details.printDetails();
            }
        });
    }

    private static void handleCommandLineArgs(String[] args, ProcessingTestSuite suite) {
        if (args[0].equals("--update")) {
            // Update specific baselines or all
            BaselineManager manager = new BaselineManager(null); // Will need tester reference
            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    System.out.println("Updating baseline: " + args[i]);
                    // Update specific test baseline
                }
            } else {
                System.out.println("Updating all baselines...");
                manager.updateAllBaselines(suite);
            }
        } else if (args[0].equals("--run")) {
            // Run specific test
            if (args.length > 1) {
                String testName = args[1];
                TestResult result = suite.runTest(testName);
                result.printResult();
            }
        }
    }

    // Example sketch: Red circle
    static class RedCircleSketch implements ProcessingSketch {
        public void setup(PApplet p) {
            p.noStroke();
        }

        public void draw(PApplet p) {
            p.background(255);
            p.fill(255, 0, 0);
            p.ellipse(p.width/2, p.height/2, 100, 100);
        }
    }

    // Example sketch: Blue square
    static class BlueSquareSketch implements ProcessingSketch {
        public void setup(PApplet p) {
            p.stroke(0);
            p.strokeWeight(2);
        }

        public void draw(PApplet p) {
            p.background(255);
            p.fill(0, 0, 255);
            p.rect(p.width/2 - 50, p.height/2 - 50, 100, 100);
        }
    }

    // Example sketch: Gradient background
    static class GradientBackgroundSketch implements ProcessingSketch {
        public void setup(PApplet p) {
            p.noStroke();
        }

        public void draw(PApplet p) {
            for (int y = 0; y < p.height; y++) {
            float inter = PApplet.map(y, 0, p.height, 0, 1);
            int c = p.lerpColor(p.color(255, 0, 0), p.color(0, 0, 255), inter);
            p.stroke(c);
            p.line(0, y, p.width, y);
        }
        }
    }

    // Example sketch: Complex pattern (more likely to have minor differences)
    static class ComplexPatternSketch implements ProcessingSketch {
        public void setup(PApplet p) {
            p.noStroke();
        }

        public void draw(PApplet p) {
            p.background(20, 20, 40);

            for (int x = 0; x < p.width; x += 15) {
            for (int y = 0; y < p.height; y += 15) {
            float noise = (float) (Math.sin(x * 0.02) * Math.cos(y * 0.02));
            int brightness = (int) ((noise + 1) * 127.5);

            p.fill(brightness, brightness * 0.7f, brightness * 1.2f);
            p.rect(x, y, 12, 12);

            // Add some text that might cause line shifts
            if (x % 60 == 0 && y % 60 == 0) {
                p.fill(255);
                p.textSize(8);
                p.text(brightness, x + 2, y + 10);
            }
        }
        }
        }
    }
}

// CI Integration helper
class ProcessingCIHelper {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--ci")) {
            runCITests();
        }  else {
            ProcessingVisualTestExamples.main(args);
        }
    }

    public static void runCITests() {
        System.out.println("Running visual tests in CI mode...");

        // Initialize comparator
        PApplet tempApplet = new PApplet();

        ImageComparator comparator = new ImageComparator(tempApplet);
        VisualTestRunner tester = new VisualTestRunner(comparator);
        ProcessingTestSuite suite = new ProcessingTestSuite(tester);

        // Add your actual test cases here
        suite.addTest("ci-test-1", new ProcessingVisualTestExamples.RedCircleSketch());
        suite.addTest("ci-test-2", new ProcessingVisualTestExamples.BlueSquareSketch());

        // Run tests
        List<TestResult> results = suite.runAll();

        // Check for failures
        boolean hasFailures = results.stream().anyMatch(r -> !r.passed && !r.isFirstRun);
        boolean hasErrors = results.stream().anyMatch(r -> r.error != null);

        if (hasFailures || hasErrors) {
            System.err.println("Visual tests failed!");
            System.exit(1);
        } else {
            System.out.println("All visual tests passed!");
            System.exit(0);
        }
    }

//    public static void updateCIBaselines(String[] testNames) {
//        System.out.println("Updating baselines in CI mode...");
//
//        // Initialize components
//        PApplet tempApplet = new PApplet();
//
//        ImageComparator comparator = new ImageComparator(tempApplet);
//        VisualTestRunner = new VisualTestRunner(comparator);
//        BaselineManager manager = new BaselineManager(tester);
//
//        if (testNames.length == 0) {
//            System.out.println("No specific tests specified, updating all...");
//            // Update all baselines - you'd need to implement this based on your test discovery
//        } else {
//            for (String testName : testNames) {
//                System.out.println("Updating baseline for: " + testName);
//                // Update specific baseline - you'd need the corresponding sketch
//            }
//        }
//
//        System.out.println("Baseline update completed!");
//    }
}