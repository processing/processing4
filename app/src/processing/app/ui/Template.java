package processing.app.ui;

public class Template {

    // New templates
    public static final String animationSketchCode =
            "void setup() {\n" +
                    "  size(500, 500);\n" +
                    "  // write code that will be called once in this function\n" +
                    "}\n\n" +
                    "void draw() {\n" +
                    "  // write code that will be called for every frame here\n" +
                    "}";

    public static final String interactiveSketchCode =
            "void setup() {\n" +
                    "  size(500, 500);\n" +
                    "  // write code that will be called once in this function\n" +
                    "}\n\n" +
                    "void draw() {\n" +
                    "  // write code that will be called for every frame here\n" +
                    "}\n\n" +
                    "void mousePressed() {\n" +
                    "  ellipse(mouseX, mouseY, 20, 20);\n" +
                    "  // write code that will run when you click\n" +
                    "}";

    public static final String fullscreenSketchCode =
            "void setup() {\n" +
                    "  fullScreen();  // create a fullscreen canvas\n" +
                    "}\n\n" +
                    "void draw() {\n" +
                    "  circle(width / 2, height / 2, height * 0.5);\n" +
                    "}";

    public static final String resizeableSketchCode =
            "void setup() {\n" +
                    "  size(500, 500);\n" +
                    "  windowResizable(true);\n" +
                    "  // allow the window to be resized\n" +
                    "}\n\n" +
                    "void draw() {\n" +
                    "  circle(width / 2, height / 2, min(width, height) * 0.5);\n" +
                    "  // draw a circle that resizes with the window\n" +
                    "}\n\n" +
                    "void windowResized() {\n" +
                    "  println(\"Window resized to: \" + width + \"x\" + height);\n" +
                    "  // this function is called whenever the window is resized\n" +
                    "}";
}