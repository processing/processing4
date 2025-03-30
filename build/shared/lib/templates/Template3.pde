// Animation Template
float x = 0;
float speed = 2;

void setup() {
  size(800, 600);
  background(255);
}

void draw() {
  background(255);
  ellipse(x, height/2, 50, 50);
  x += speed;
  if (x > width || x < 0) {
    speed *= -1;
  }
}