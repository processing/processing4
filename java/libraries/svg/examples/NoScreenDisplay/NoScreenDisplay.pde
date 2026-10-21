/**
 * SVG Export (No Screen Display)
 * 
 * This example draws a single frame to a SVG file and quits. 
 * (Note that no display window will open; this helps when you're 
 * trying to create massive SVG images that are far larger than 
 * the screen size.)
 */

import processing.svg.*;

void setup() {
  size(400, 400, SVG, "filename.svg");
}

void draw() {
  // Draw something good here
  line(0, 0, width/2, height);

  // Exit the program
  println("Finished.");
  exit();
}
