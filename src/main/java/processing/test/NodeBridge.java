package processing.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Bridge class to execute Node.js functions from Java/Processing
 */
public class NodeBridge {
    private static final String NODE_SCRIPT_PATH = "src/main/js/index.js";
    private final Gson gson = new Gson();

    /**
     * Execute a Node.js command and return the result
     */
    private String executeNodeCommand(String command, String data) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("node", NODE_SCRIPT_PATH, command, data);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Node.js process failed with exit code: " + exitCode);
        }

        return output.toString();
    }

    /**
     * Shuffle an array using lodash
     */
    public List<Integer> shuffleArray(List<Integer> array) {
        try {
            String jsonData = gson.toJson(array);
            String result = executeNodeCommand("shuffle", jsonData);

            JsonArray jsonArray = gson.fromJson(result, JsonArray.class);
            List<Integer> shuffled = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                shuffled.add(element.getAsInt());
            }
            return shuffled;
        } catch (Exception e) {
            System.err.println("Error shuffling array: " + e.getMessage());
            return array; // return original on error
        }
    }

    /**
     * Get statistical information about a number array
     */
    public ArrayStats getArrayStats(List<Double> numbers) {
        try {
            String jsonData = gson.toJson(numbers);
            String result = executeNodeCommand("stats", jsonData);

            JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
            return new ArrayStats(
                    jsonObject.get("mean").getAsDouble(),
                    jsonObject.get("sum").getAsDouble(),
                    jsonObject.get("min").getAsDouble(),
                    jsonObject.get("max").getAsDouble(),
                    jsonObject.get("size").getAsInt()
            );
        } catch (Exception e) {
            System.err.println("Error getting array stats: " + e.getMessage());
            return null;
        }
    }

    /**
     * Capitalize words using lodash
     */
    public String capitalizeWords(String text) {
        try {
            String jsonData = gson.toJson(text);
            String result = executeNodeCommand("capitalize", jsonData);
            return gson.fromJson(result, String.class);
        } catch (Exception e) {
            System.err.println("Error capitalizing words: " + e.getMessage());
            return text; // return original on error
        }
    }

    /**
     * Generate a color palette for Processing
     */
    public List<ColorRGB> generateColorPalette(int count) {
        try {
            String jsonData = String.valueOf(count);
            String result = executeNodeCommand("colors", jsonData);

            JsonArray jsonArray = gson.fromJson(result, JsonArray.class);
            List<ColorRGB> colors = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                JsonObject colorObj = element.getAsJsonObject();
                colors.add(new ColorRGB(
                        colorObj.get("r").getAsInt(),
                        colorObj.get("g").getAsInt(),
                        colorObj.get("b").getAsInt()
                ));
            }
            return colors;
        } catch (Exception e) {
            System.err.println("Error generating color palette: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Helper classes for structured data
    public static class ArrayStats {
        public final double mean;
        public final double sum;
        public final double min;
        public final double max;
        public final int size;

        public ArrayStats(double mean, double sum, double min, double max, int size) {
            this.mean = mean;
            this.sum = sum;
            this.min = min;
            this.max = max;
            this.size = size;
        }

        @Override
        public String toString() {
            return String.format("Stats{mean=%.2f, sum=%.2f, min=%.2f, max=%.2f, size=%d}",
                    mean, sum, min, max, size);
        }
    }

    public static class ColorRGB {
        public final int r, g, b;

        public ColorRGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public String toString() {
            return String.format("RGB(%d, %d, %d)", r, g, b);
        }
    }
}