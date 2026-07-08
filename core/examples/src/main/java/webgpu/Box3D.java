package webgpu;

import processing.core.PApplet;

public class Box3D extends PApplet {

    float angle = 0;

    public void settings() {
        size(400, 400, WEBGPU);
    }

    public void setup() {
        perspective(PI/3, (float)width/height, 0.1f, 1000);
        camera(200, 200, 300, 0, 0, 0, 0, 1, 0);
    }

    public void draw() {
        background(26, 26, 38);

        pushMatrix();
        rotateY(angle);
        rotateX(angle * 0.7f);
        fill(200, 100, 100);
        box(100);
        popMatrix();

        angle += 0.02f;
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(Box3D.class.getName());
    }
}
