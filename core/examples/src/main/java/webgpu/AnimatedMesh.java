package webgpu;

import processing.core.PApplet;

public class AnimatedMesh extends PApplet {

    int gridSize = 20;
    float spacing = 10;
    float time = 0;

    public void settings() {
        size(600, 600, WEBGPU);
    }

    public void setup() {
        perspective(PI/3, (float)width/height, 0.1f, 1000);
        camera(150, 150, 150, 0, 0, 0, 0, 1, 0);
    }

    public void draw() {
        background(13, 13, 26);

        float offset = (gridSize * spacing) / 2.0f;

        beginShape(TRIANGLES);
        for (int z = 0; z < gridSize - 1; z++) {
            for (int x = 0; x < gridSize - 1; x++) {
                float px0 = x * spacing - offset;
                float pz0 = z * spacing - offset;
                float px1 = (x + 1) * spacing - offset;
                float pz1 = (z + 1) * spacing - offset;

                float y00 = wave(px0, pz0);
                float y10 = wave(px1, pz0);
                float y01 = wave(px0, pz1);
                float y11 = wave(px1, pz1);

                fill(x * 255.0f / gridSize, 128, z * 255.0f / gridSize);
                normal(0, 1, 0);

                vertex(px0, y00, pz0);
                vertex(px0, y01, pz1);
                vertex(px1, y10, pz0);

                vertex(px1, y10, pz0);
                vertex(px0, y01, pz1);
                vertex(px1, y11, pz1);
            }
        }
        endShape();

        time += 0.05f;
    }

    float wave(float x, float z) {
        return sin(x * 0.1f + time) * cos(z * 0.1f + time) * 20;
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(AnimatedMesh.class.getName());
    }
}
