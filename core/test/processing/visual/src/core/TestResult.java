package processing.visual.src.core;

// Enhanced test result with detailed information 
public class TestResult {
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
