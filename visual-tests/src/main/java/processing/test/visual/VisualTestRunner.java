import processing.core.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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

    // Replace loadBaseline method:
    private PImage loadBaseline(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("loadBaseline: File doesn't exist: " + file.getAbsolutePath());
            return null;
        }

        try {
            System.out.println("loadBaseline: Loading from " + file.getAbsolutePath());

            // Use Java ImageIO instead of PApplet
            BufferedImage img = ImageIO.read(file);

            if (img == null) {
                System.out.println("loadBaseline: ImageIO returned null");
                return null;
            }

            // Convert BufferedImage to PImage
            PImage pImg = new PImage(img.getWidth(), img.getHeight(), PImage.RGB);
            img.getRGB(0, 0, pImg.width, pImg.height, pImg.pixels, 0, pImg.width);
            pImg.updatePixels();

            System.out.println("loadBaseline: ✓ Loaded " + pImg.width + "x" + pImg.height);
            return pImg;

        } catch (Exception e) {
            System.err.println("loadBaseline: Error loading image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Replace saveBaseline method:
    private void saveBaseline(String testName, PImage image) {
        String path = getBaselinePath(testName);

        if (image == null) {
            System.out.println("saveBaseline: ✗ Image is null!");
            return;
        }

        try {
            // Convert PImage to BufferedImage
            BufferedImage bImg = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB);
            image.loadPixels();
            bImg.setRGB(0, 0, image.width, image.height, image.pixels, 0, image.width);

            // Use Java ImageIO to save
            File outputFile = new File(path);
            outputFile.getParentFile().mkdirs(); // Ensure directory exists

            ImageIO.write(bImg, "PNG", outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class SketchRunner extends PApplet {

    private ProcessingSketch userSketch;
    private TestConfig config;
    private PImage capturedImage;
    private volatile boolean rendered = false;

    public SketchRunner(ProcessingSketch userSketch, TestConfig config) {
        this.userSketch = userSketch;
        this.config = config;
    }

    public void settings() {
        size(config.width, config.height);
    }

    public void setup() {
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
            userSketch.draw(this);
            capturedImage = get();
            rendered = true;
        }
    }

    public void run() {
        String[] args = {"SketchRunner"};
        PApplet.runSketch(args, this);

        // Simple polling with timeout
        int maxWait = 100; // 10 seconds max
        int waited = 0;

        while (!rendered && waited < maxWait) {
            try {
                Thread.sleep(100);
                waited++;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Additional wait time
        try {
            Thread.sleep(config.renderWaitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (surface != null) {
            surface.setVisible(false);
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
    public long renderWaitTime = 100; // milliseconds
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