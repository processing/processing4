enum Direction {
  NORTH, SOUTH, EAST, WEST
}

void setup() {
  size(100, 100);
  Direction d = Direction.NORTH;
  println(d);
}

void draw() {
  background(255);
}