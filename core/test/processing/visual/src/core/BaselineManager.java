package processing.visual.src.core;

import processing.core.PImage;

import java.util.List;

// Baseline manager for updating reference images
public class BaselineManager {
    private VisualTestRunner tester;

    public BaselineManager(VisualTestRunner tester) {
        this.tester = tester;
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

    private String detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) return "darwin";
        if (os.contains("win")) return "win32";
        return "linux";
    }
}
