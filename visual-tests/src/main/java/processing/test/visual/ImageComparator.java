package processing.test.visual;

import processing.core.*;
import java.util.*;

class ComparisonResult {
    public boolean passed;
    public double mismatchRatio;
    public boolean isFirstRun;
    public PImage diffImage;
    public ComparisonDetails details;

    public ComparisonResult(boolean passed, double mismatchRatio) {
        this.passed = passed;
        this.mismatchRatio = mismatchRatio;
        this.isFirstRun = false;
    }

    public ComparisonResult(boolean passed, PImage diffImage, ComparisonDetails details) {
        this.passed = passed;
        this.diffImage = diffImage;
        this.details = details;
        this.mismatchRatio = details != null ? (double) details.significantDiffPixels / (diffImage.width * diffImage.height) : 0.0;
        this.isFirstRun = false;
    }

    public static ComparisonResult createFirstRun() {
        ComparisonResult result = new ComparisonResult(false, 0.0);
        result.isFirstRun = true;
        return result;
    }

    public void saveDiffImage(String filePath) {
        if (diffImage != null) {
            diffImage.save(filePath);
            System.out.println("Diff image saved: " + filePath);
        }
    }
}

class ComparisonDetails {
    public int totalDiffPixels;
    public int significantDiffPixels;
    public List<ClusterInfo> clusters;

    public ComparisonDetails(int totalDiffPixels, int significantDiffPixels, List<ClusterInfo> clusters) {
        this.totalDiffPixels = totalDiffPixels;
        this.significantDiffPixels = significantDiffPixels;
        this.clusters = clusters;
    }

    public void printDetails() {
        System.out.println("  Total diff pixels: " + totalDiffPixels);
        System.out.println("  Significant diff pixels: " + significantDiffPixels);
        System.out.println("  Clusters found: " + clusters.size());

        long lineShiftClusters = clusters.stream().filter(c -> c.isLineShift).count();
        if (lineShiftClusters > 0) {
            System.out.println("  Line shift clusters (ignored): " + lineShiftClusters);
        }

        // Print cluster details
        for (int i = 0; i < clusters.size(); i++) {
            ClusterInfo cluster = clusters.get(i);
            System.out.println("    Cluster " + (i+1) + ": size=" + cluster.size +
                    ", lineShift=" + cluster.isLineShift);
        }
    }
}

// Individual cluster information
class ClusterInfo {
    public int size;
    public List<Point2D> pixels;
    public boolean isLineShift;

    public ClusterInfo(int size, List<Point2D> pixels, boolean isLineShift) {
        this.size = size;
        this.pixels = pixels;
        this.isLineShift = isLineShift;
    }
}

// Simple 2D point
class Point2D {
    public int x, y;

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

// Interface for pixel matching algorithms
interface PixelMatchingAlgorithm {
    ComparisonResult compare(PImage baseline, PImage actual, double threshold);
}

// Your sophisticated pixel matching algorithm
public class ImageComparator implements PixelMatchingAlgorithm {

    // Algorithm constants
    private static final int MAX_SIDE = 400;
    private static final int BG_COLOR = 0xFFFFFFFF; // White background
    private static final int MIN_CLUSTER_SIZE = 4;
    private static final int MAX_TOTAL_DIFF_PIXELS = 40;
    private static final double DEFAULT_THRESHOLD = 0.5;
    private static final double ALPHA = 0.1;

    private PApplet p; // Reference to PApplet for PImage creation

    public ImageComparator(PApplet p) {
        this.p = p;
    }

    @Override
    public ComparisonResult compare(PImage baseline, PImage actual, double threshold) {
        if (baseline == null || actual == null) {
            return new ComparisonResult(false, 1.0);
        }

        try {
            return performComparison(baseline, actual, threshold);
        } catch (Exception e) {
            System.err.println("Comparison failed: " + e.getMessage());
            return new ComparisonResult(false, 1.0);
        }
    }

    private ComparisonResult performComparison(PImage baseline, PImage actual, double threshold) {
        // Calculate scaling
        double scale = Math.min(
                (double) MAX_SIDE / baseline.width,
        (double) MAX_SIDE / baseline.height
        );

        double ratio = (double) baseline.width / baseline.height;
        boolean narrow = ratio != 1.0;
        if (narrow) {
            scale *= 2;
        }

        // Resize images
        PImage scaledActual = resizeImage(actual, scale);
        PImage scaledBaseline = resizeImage(baseline, scale);

        // Ensure both images have the same dimensions
        int width = scaledBaseline.width;
        int height = scaledBaseline.height;

        // Create canvases with background color
        PImage actualCanvas = createCanvasWithBackground(scaledActual, width, height);
        PImage baselineCanvas = createCanvasWithBackground(scaledBaseline, width, height);

        // Create diff output canvas
        PImage diffCanvas = p.createImage(width, height, PImage.RGB);

        // Run pixelmatch equivalent
        int diffCount = pixelmatch(actualCanvas, baselineCanvas, diffCanvas, width, height, DEFAULT_THRESHOLD);

        // If no differences, return early
        if (diffCount == 0) {
            return new ComparisonResult(true, diffCanvas, null);
        }

        // Post-process to identify and filter out isolated differences
        Set<Integer> visited = new HashSet<>();
        List<ClusterInfo> clusterSizes = new ArrayList<>();

        for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
        int pos = y * width + x;

        // If this is a diff pixel and not yet visited
        if (isDiffPixel(diffCanvas, x, y) && !visited.contains(pos)) {
            ClusterInfo clusterInfo = findClusterSize(diffCanvas, x, y, width, height, visited);
            clusterSizes.add(clusterInfo);
        }
    }
    }

        // Determine if the differences are significant
        List<ClusterInfo> nonLineShiftClusters = clusterSizes.stream()
            .filter(cluster -> !cluster.isLineShift && cluster.size >= MIN_CLUSTER_SIZE)
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // Calculate significant differences excluding line shifts
        int significantDiffPixels = nonLineShiftClusters.stream()
            .mapToInt(cluster -> cluster.size)
        .sum();

        // Determine test result
        boolean passed = diffCount == 0 ||
        significantDiffPixels == 0 ||
                (significantDiffPixels <= MAX_TOTAL_DIFF_PIXELS && nonLineShiftClusters.size() <= 2);

        ComparisonDetails details = new ComparisonDetails(diffCount, significantDiffPixels, clusterSizes);

        return new ComparisonResult(passed, diffCanvas, details);
    }

    private PImage resizeImage(PImage image, double scale) {
        int newWidth = (int) Math.ceil(image.width * scale);
        int newHeight = (int) Math.ceil(image.height * scale);

        PImage resized = p.createImage(newWidth, newHeight, PImage.RGB);
        resized.copy(image, 0, 0, image.width, image.height, 0, 0, newWidth, newHeight);

        return resized;
    }

    private PImage createCanvasWithBackground(PImage image, int width, int height) {
        PImage canvas = p.createImage(width, height, PImage.RGB);

        // Fill with background color (white)
        canvas.loadPixels();
        for (int i = 0; i < canvas.pixels.length; i++) {
        canvas.pixels[i] = BG_COLOR;
    }
        canvas.updatePixels();

        // Draw the image on top
        canvas.copy(image, 0, 0, image.width, image.height, 0, 0, image.width, image.height);

        return canvas;
    }

    private int pixelmatch(PImage actual, PImage expected, PImage diff, int width, int height, double threshold) {
        int diffCount = 0;

        actual.loadPixels();
        expected.loadPixels();
        diff.loadPixels();

        for (int i = 0; i < actual.pixels.length; i++) {
        int actualColor = actual.pixels[i];
        int expectedColor = expected.pixels[i];

        double delta = colorDelta(actualColor, expectedColor);

        if (delta > threshold) {
            // Mark as different (bright red pixel)
            diff.pixels[i] = 0xFFFF0000; // Red
            diffCount++;
        } else {
            // Mark as same (dimmed version of actual image)
            int dimColor = dimColor(actualColor, ALPHA);
            diff.pixels[i] = dimColor;
        }
    }

        diff.updatePixels();
        return diffCount;
    }

    private double colorDelta(int color1, int color2) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;
        int da = a1 - a2;

        return Math.sqrt(dr * dr + dg * dg + db * db + da * da) / 255.0;
    }

    private int dimColor(int color, double alpha) {
        int r = (int) (((color >> 16) & 0xFF) * alpha);
        int g = (int) (((color >> 8) & 0xFF) * alpha);
        int b = (int) ((color & 0xFF) * alpha);
        int a = (int) (255 * alpha);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private boolean isDiffPixel(PImage image, int x, int y) {
        if (x < 0 || x >= image.width || y < 0 || y >= image.height) return false;

        image.loadPixels();
        int color = image.pixels[y * image.width + x];

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        return r == 255 && g == 0 && b == 0;
    }

    private ClusterInfo findClusterSize(PImage diffImage, int startX, int startY, int width, int height, Set<Integer> visited) {
        List<Point2D> queue = new ArrayList<>();
        queue.add(new Point2D(startX, startY));

        int size = 0;
        List<Point2D> clusterPixels = new ArrayList<>();

        while (!queue.isEmpty()) {
            Point2D point = queue.remove(0);
            int pos = point.y * width + point.x;

            // Skip if already visited
            if (visited.contains(pos)) continue;

            // Skip if not a diff pixel
            if (!isDiffPixel(diffImage, point.x, point.y)) continue;

            // Mark as visited
            visited.add(pos);
            size++;
            clusterPixels.add(point);

            // Add neighbors to queue
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = point.x + dx;
                int ny = point.y + dy;

                // Skip if out of bounds
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

                // Skip if already visited
                int npos = ny * width + nx;
                if (!visited.contains(npos)) {
                    queue.add(new Point2D(nx, ny));
                }
            }
            }
        }

        // Determine if this is a line shift
        boolean isLineShift = detectLineShift(clusterPixels, diffImage, width, height);

        return new ClusterInfo(size, clusterPixels, isLineShift);
    }

    private boolean detectLineShift(List<Point2D> clusterPixels, PImage diffImage, int width, int height) {
        if (clusterPixels.isEmpty()) return false;

        int linelikePixels = 0;

        for (Point2D pixel : clusterPixels) {
        int neighbors = 0;
        for (int dy = -1; dy <= 1; dy++) {
        for (int dx = -1; dx <= 1; dx++) {
        if (dx == 0 && dy == 0) continue; // Skip self

        int nx = pixel.x + dx;
        int ny = pixel.y + dy;

        // Skip if out of bounds
        if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

        // Check if neighbor is a diff pixel
        if (isDiffPixel(diffImage, nx, ny)) {
            neighbors++;
        }
    }
    }

        // Line-like pixels typically have 1-2 neighbors
        if (neighbors <= 2) {
            linelikePixels++;
        }
    }

        // If most pixels (>80%) in the cluster have ≤2 neighbors, it's likely a line shift
        return (double) linelikePixels / clusterPixels.size() > 0.8;
    }

    // Configuration methods
    public ImageComparator setMaxSide(int maxSide) {
        // For future configurability
        return this;
    }

    public ImageComparator setMinClusterSize(int minClusterSize) {
        // For future configurability
        return this;
    }

    public ImageComparator setMaxTotalDiffPixels(int maxTotalDiffPixels) {
        // For future configurability
        return this;
    }
}

// Utility class for algorithm configuration
class ComparatorConfig {
    public int maxSide = 400;
    public int minClusterSize = 4;
    public int maxTotalDiffPixels = 40;
    public double threshold = 0.5;
    public double alpha = 0.1;
    public int backgroundColor = 0xFFFFFFFF;

    public ComparatorConfig() {}

    public ComparatorConfig(int maxSide, int minClusterSize, int maxTotalDiffPixels) {
        this.maxSide = maxSide;
        this.minClusterSize = minClusterSize;
        this.maxTotalDiffPixels = maxTotalDiffPixels;
    }
}