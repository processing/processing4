import processing.core.PApplet;

public class Basic extends PApplet {
    // These use the Interface name so we can switch between them easily
    Feature particleSystem;
    Feature mazeSystem;
    Feature activeFeature;

    public void settings() {
        size(800, 800);
    }

    public void setup() {
        // HSB mode: Hue (0-360), Saturation (0-100), Brightness (0-100)
        colorMode(HSB, 360, 100, 100);

        // Initialize your features
        particleSystem = new ParticleSprayingFountain(this);
        mazeSystem = new maze(this);

        activeFeature = null; // Start at the menu
        textFont(createFont("Arial Bold", 20));
    }

    public void draw() {
        background(0, 0, 10); // Dark background

        if (activeFeature == null) {
            cursor(ARROW); // Ensure arrow cursor on menu
            drawMenu();
        } else {
            // Run the active feature logic
            activeFeature.update();
            activeFeature.display();
            activeFeature.handleMouse();

            // Draw the instruction bar ON TOP of everything else
            drawInstructionBar();
        }
    }

    void drawInstructionBar() {
        // Push style saves current colors/settings so we don't mess up the feature
        pushStyle();

        // 1. Draw Bar
        rectMode(CORNER);
        noStroke();
        fill(0, 0, 0, 200); // Dark semi-transparent
        rect(0, height - 50, width, 50);

        // 2. Draw Text
        fill(0, 0, 100); // Pure White in HSB
        textAlign(CENTER, CENTER);
        textSize(16);

        String info = activeFeature.getInstructions();
        if (info != null) {
            text(info, width / 2f, height - 25);
        }

        popStyle(); // Restore feature's original styles
    }

    void drawMenu() {
        // Animated Title
        float h = (frameCount % 360);
        textAlign(CENTER, CENTER);
        textSize(50);
        fill(h, 70, 100);
        text("PROJECT HUB", width/2f, 150);

        // Button 1: Particle Fountain
        boolean hover1 = mouseInRect(width/2f - 150, 300, 300, 60);
        drawButton(width/2f - 150, 300, 300, 60, "1: Particle Fountain", hover1, 200);

        // Button 2: Maze Generator
        boolean hover2 = mouseInRect(width/2f - 150, 400, 300, 60);
        drawButton(width/2f - 150, 400, 300, 60, "2: Maze Generator", hover2, 140);

        // Footer Info
        fill(0, 0, 60);
        textSize(14);
        text("Click a button or press '1' / '2' to start", width/2f, 550);
        text("Press 'M' to return to menu at any time", width/2f, 580);
    }

    void drawButton(float x, float y, float w, float h, String label, boolean hover, float hue) {
        pushStyle();
        strokeWeight(2);
        rectMode(CORNER);

        if (hover) {
            fill(hue, 80, 80);
            stroke(0, 0, 100);
            cursor(HAND);
        } else {
            fill(hue, 60, 40);
            stroke(hue, 80, 60);
        }

        rect(x, y, w, h, 15);

        fill(0, 0, 100);
        textAlign(CENTER, CENTER);
        textSize(20);
        text(label, x + w/2f, y + h/2f);
        popStyle();
    }

    boolean mouseInRect(float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public void mousePressed() {
        if (activeFeature == null) {
            if (mouseInRect(width/2f - 150, 300, 300, 60)) activeFeature = particleSystem;
            if (mouseInRect(width/2f - 150, 400, 300, 60)) activeFeature = mazeSystem;
        }
    }

    public void keyPressed() {
        if (key == '1') activeFeature = particleSystem;
        if (key == '2') activeFeature = mazeSystem;
        if (key == 'm' || key == 'M') activeFeature = null;

        if (activeFeature != null) {
            activeFeature.handleKeys();
        }
    }

    public static void main(String[] args) {
        PApplet.main("Basic");
    }
}