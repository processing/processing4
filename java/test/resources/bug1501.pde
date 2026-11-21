float to;
Module module;

void setup() {
  size(400, 400);
  int open = 1;
  String with = "with";
  to = 5.0;
  module = new Module();
  int transitive = open + 2;
  println(to, with, transitive);
  provides();
}

void provides() {
  int record = 2;
  int permits = record + 1;
  println(permits);
}

class Module {
}
