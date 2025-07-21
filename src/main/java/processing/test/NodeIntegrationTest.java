package processing.test;

import java.util.Arrays;
import java.util.List;
import processing.test.NodeBridge;
import processing.test.NodeBridge.ArrayStats;
import processing.test.NodeBridge.ColorRGB;

/**
 * Test class to demonstrate Node.js integration with Processing
 */
public class NodeIntegrationTest {

    public static void main(String[] args) {
        NodeBridge bridge = new NodeBridge();

        System.out.println("=== Testing Node.js Integration with Processing 4 ===\n");

        // Test 1: Array shuffling
        System.out.println("1. Testing array shuffling with lodash:");
        List<Integer> originalArray = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("Original: " + originalArray);
        List<Integer> shuffled = bridge.shuffleArray(originalArray);
        System.out.println("Shuffled: " + shuffled);
        System.out.println();

        // Test 2: Statistical analysis
        System.out.println("2. Testing statistical analysis:");
        List<Double> numbers = Arrays.asList(10.5, 20.3, 15.7, 30.1, 25.9, 12.4, 18.6);
        System.out.println("Numbers: " + numbers);
        ArrayStats stats = bridge.getArrayStats(numbers);
        if (stats != null) {
            System.out.println("Statistics: " + stats);
        }
        System.out.println();

        // Test 3: String manipulation
        System.out.println("3. Testing string capitalization:");
        String originalText = "hello world from PROCESSING and node.js";
        System.out.println("Original: " + originalText);
        String capitalized = bridge.capitalizeWords(originalText);
        System.out.println("Capitalized: " + capitalized);
        System.out.println();

        // Test 4: Color palette generation for Processing sketches
        System.out.println("4. Testing color palette generation:");
        List<ColorRGB> palette = bridge.generateColorPalette(5);
        System.out.println("Generated color palette:");
        for (int i = 0; i < palette.size(); i++) {
            ColorRGB color = palette.get(i);
            System.out.println("  Color " + (i + 1) + ": " + color);
        }
        System.out.println();

        System.out.println("=== All tests completed! ===");
    }
}