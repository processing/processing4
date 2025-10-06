package visual.base;

import org.junit.jupiter.api.*;
import processing.core.*;
import static org.junit.jupiter.api.Assertions.*;
import processing.test.visual.*;
import java.nio.file.*;
import java.io.File;

/**
 * Base class for Processing visual tests using JUnit 5
 */
public abstract class VisualTest {

    protected static VisualTestRunner testRunner;
    protected static ImageComparator comparator;

    @BeforeAll
    public static void setupTestRunner() {
        PApplet tempApplet = new PApplet();
        comparator = new ImageComparator(tempApplet);
        testRunner = new VisualTestRunner(comparator);

        System.out.println("Visual test runner initialized");
    }

    /**
     * Helper method to run a visual test
     */
    protected void assertVisualMatch(String testName, ProcessingSketch sketch) {
        assertVisualMatch(testName, sketch, new TestConfig());
    }

    protected void assertVisualMatch(String testName, ProcessingSketch sketch, TestConfig config) {
        TestResult result = testRunner.runVisualTest(testName, sketch, config);

        // Print result for debugging
        result.printResult();

        // Handle different result types
        if (result.isFirstRun) {
            // First run - baseline created, mark as skipped
            Assumptions.assumeTrue(false, "Baseline created for " + testName + ". Run tests again to verify.");
        } else if (result.error != null) {
            fail("Test error: " + result.error);
        } else {
            // Assert that the test passed
            Assertions.assertTrue(result.passed,
                    String.format("Visual test '%s' failed with mismatch ratio: %.4f%%",
                            testName, result.mismatchRatio * 100));
        }
    }

    /**
     * Update baseline for a specific test (useful for maintenance)
     */
    protected void updateBaseline(String testName, ProcessingSketch sketch, TestConfig config) {
        BaselineManager manager = new BaselineManager(testRunner);
        manager.updateBaseline(testName, sketch, config);
    }
}