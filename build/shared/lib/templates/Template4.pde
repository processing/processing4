// Interactive Template
void setup() {
  size(800, 600);
  background(255);
}

void draw() {
  background(255);
  fill(0, 0, 150);
  ellipse(mouseX, mouseY, 50, 50);
}

void mousePressed() {
  background(0, 255, 0);
}