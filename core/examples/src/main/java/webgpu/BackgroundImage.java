package webgpu;

import processing.core.PApplet;
import processing.core.PImage;

public class BackgroundImage extends PApplet {

    PImage img;

    public void settings() {
        size(400, 400, WEBGPU);
    }

    public void setup() {
        img = createImage(400, 400, RGB);
        img.loadPixels();
        for (int y = 0; y < img.height; y++) {
            for (int x = 0; x < img.width; x++) {
                int r = (int) (x * 255.0 / img.width);
                int g = (int) (y * 255.0 / img.height);
                int b = 128;
                img.pixels[y * img.width + x] = color(r, g, b);
            }
        }
        img.updatePixels();
    }

    public void draw() {
        background(img);
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(BackgroundImage.class.getName());
    }
}
