import processing.core.PApplet;

public class WebGPU extends PApplet {
    public void settings() {
        size(600, 400, WEBGPU);
    }

    public void draw() {
        background(200);

        noStroke();
        fill(255, 0, 0);
        rect(50, 50, 80, 80);

        fill(0, 255, 0);
        rect(150, 50, 80, 80);

        fill(0, 0, 255);
        rect(250, 50, 80, 80);

        fill(255, 0, 0, 128);
        rect(50, 150, 80, 80);

        fill(0, 255, 0, 128);
        rect(150, 150, 80, 80);

        fill(0, 0, 255, 128);
        rect(250, 150, 80, 80);

        stroke(0);
        strokeWeight(4);
        fill(255, 200, 0);
        rect(50, 250, 80, 80);

        fill(200, 0, 255);
        rect(150, 250, 80, 80);

        noFill();
        stroke(255, 0, 255);
        strokeWeight(6);
        rect(250, 250, 80, 80);

        noStroke();
        fill(255, 0, 0, 100);
        rect(400, 100, 100, 100);

        fill(0, 255, 0, 100);
        rect(440, 140, 100, 100);

        fill(0, 0, 255, 100);
        rect(420, 180, 100, 100);
    }

    public static void main(String[] args) {
        PApplet.disableAWT = true;
        PApplet.main(WebGPU.class.getName());
    }
}
