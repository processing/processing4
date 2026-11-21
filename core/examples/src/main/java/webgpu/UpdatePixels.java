package webgpu;

import processing.core.PApplet;

public class UpdatePixels extends PApplet {

    static final int RECT_W = 10;
    static final int RECT_H = 10;

    boolean firstFrame = true;

    public void settings() {
        size(100, 100, WEBGPU);
    }

    public void draw() {
        background(0);  // Black background

        loadPixels();

        for (int y = 20; y < 20 + RECT_H; y++) {
            for (int x = 20; x < 20 + RECT_W; x++) {
                pixels[y * width + x] = color(255, 0, 0);
            }
        }

        for (int y = 60; y < 60 + RECT_H; y++) {
            for (int x = 60; x < 60 + RECT_W; x++) {
                pixels[y * width + x] = color(0, 0, 255);
            }
        }

        updatePixels();

        if (firstFrame) {
            firstFrame = false;

            println("Total pixels: " + pixels.length);

            for (int y = 0; y < height; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    int idx = y * width + x;
                    int pixel = pixels[idx];
                    float r = red(pixel);
                    float b = blue(pixel);
                    float a = alpha(pixel);

                    if (r > 127) {
                        row.append("R");
                    } else if (b > 127) {
                        row.append("B");
                    } else if (a > 127) {
                        row.append(".");
                    } else {
                        row.append(" ");
                    }
                }
                println(row.toString());
            }

            println("\nSample pixels:");
            println("(25, 25): " + hex(pixels[25 * width + 25]));
            println("(65, 65): " + hex(pixels[65 * width + 65]));
            println("(0, 0): " + hex(pixels[0]));
            println("(50, 50): " + hex(pixels[50 * width + 50]));
        }
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(UpdatePixels.class.getName());
    }
}
