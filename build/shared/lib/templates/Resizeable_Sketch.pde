void setup() {
  size(500, 500);
  windowResizable(true);
  // allow the window to be resized
}

void draw() {
  circle(width / 2, height / 2, min(width, height) * 0.5);
  // draw a circle that resizes with the window
}

void windowResized() {
  println("Window resized to: " + width + "x" + height);
  // this function is called whenever the window is resized
}