package webgpu;

import processing.core.PApplet;

public class Transforms extends PApplet {

    float t = 0;

    public void settings() {
        size(400, 400, WEBGPU);
    }

    public void draw() {
        background(26);

        noStroke();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                pushMatrix();

                translate(50 + j * 100, 50 + i * 100);

                float angle = t + (i + j) * PI / 8.0f;
                rotate(angle);

                float s = 0.8f + sin(t * 2.0f + (i * j)) * 0.2f;
                scale(s, s);

                float r = j / 3.0f;
                float g = i / 3.0f;
                fill(r * 255, g * 255, 204);

                rect(-20, -20, 40, 40);

                popMatrix();
            }
        }

        t += 0.02f;
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(Transforms.class.getName());
    }
}
