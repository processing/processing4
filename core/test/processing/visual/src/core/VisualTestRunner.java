package processing.visual.src.core;

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
        this.screenshotDir = "test/processing/visual/__screenshots__";
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
        String diffPath;
        if (sanitizedName.contains("/")) {
            diffPath = "test/processing/visual/diff_" + sanitizedName.replace("/", "_") + "-" + platform + ".png";
        } else {
            diffPath = "test/processing/visual/diff_" + sanitizedName + "-" + platform + ".png";
        }

        File diffFile = new File(diffPath);
        diffFile.getParentFile().mkdirs();

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
        String sanitizedName = testName.replaceAll("[^a-zA-Z0-9-_/]", "-");

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

            // Create File object and ensure parent directories exist
            File outputFile = new File(path);
            outputFile.getParentFile().mkdirs(); // This creates nested directories

            // Use Java ImageIO to save
            ImageIO.write(bImg, "PNG", outputFile);

            System.out.println("Baseline saved: " + path);

        } catch (Exception e) {
            System.err.println("Failed to save baseline: " + path);
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
            noLoop();
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
