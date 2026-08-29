/**
 * SVG Export (With Screen Display)
 * 
 * To draw to the screen while also saving an SVG, use the 
 * beginRecord() and endRecord() functions. Unlike the PDF 
 * renderer, the SVG renderer will only save the final frame 
 * of a sequence. This is slower, but is useful when you need 
 * to see what you're working on as it saves.
 */

import processing.svg.*;

void setup() {
  size(400, 400);
  noLoop();
  beginRecord(SVG, "filename.svg");
}

void draw() {
  // Draw something good here
  line(0, 0, width/2, height);

  endRecord();
}
