package processing.test.visual;

// Test configuration class
public class TestConfig {
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
