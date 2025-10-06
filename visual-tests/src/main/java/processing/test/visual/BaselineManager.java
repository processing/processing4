package processing.test.visual;

import processing.core.PImage;

import java.util.List;

// Baseline manager for updating reference images
public class BaselineManager {
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
