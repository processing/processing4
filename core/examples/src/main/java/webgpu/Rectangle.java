package webgpu;

import processing.core.PApplet;

public class Rectangle extends PApplet {

    public void settings() {
        size(400, 400, WEBGPU);
    }

    public void draw() {
        background(51);

        fill(255);
        noStroke();
        rect(10, 10, 100, 100);
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(Rectangle.class.getName());
    }
}
