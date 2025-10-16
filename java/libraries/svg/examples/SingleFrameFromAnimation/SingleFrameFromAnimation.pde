/**
 * Single Frame from an Animation (With Screen Display)
 * 
 * It's also possible to save one frame from a program with 
 * moving elements. Create a boolean variable to turn the SVG 
 * recording process on and off.
 */

import processing.svg.*;

boolean record;

void setup() {
  size(400, 400);
}

void draw() {
  if (record) {
    // Note that #### will be replaced with the frame number. Fancy!
    beginRecord(SVG, "frame-####.svg");
  }

  // Draw something good here
  background(255);
  line(mouseX, mouseY, width/2, height/2);

  if (record) {
    endRecord();
    record = false;
  }
}

// Use a mouse press so thousands of files aren't created
void mousePressed() {
  record = true;
}
