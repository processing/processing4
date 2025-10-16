/**
 * SVG Files from 3D Geometry (With Screen Display)
 * 
 * To create vectors from 3D data, use the beginRaw() and 
 * endRaw() commands. These commands will grab the shape data 
 * just before it is rendered to the screen. At this stage, 
 * your entire scene is nothing but a long list of lines and 
 * triangles. This means that a shape created with sphere() 
 * method will be made up of hundreds of triangles, rather 
 * than a single object.
 * 
 * When using beginRaw() and endRaw(), it's possible to write 
 * to either a 2D or 3D renderer. For instance, beginRaw() 
 * with the SVG library will write the geometry as flattened 
 * triangles and lines.
 */

import processing.svg.*;

boolean record;

void setup() {
  size(500, 500, P3D);
}

void draw() {
  if (record) {
    beginRaw(SVG, "output.svg");
  }

  // Do all your drawing here
  background(204);
  translate(width/2, height/2, -200);
  rotateZ(0.2);
  rotateY(mouseX/500.0);
  box(200);

  if (record) {
    endRaw();
    record = false;
  }
}

// Hit 'r' to record a single frame
void keyPressed() {
  if (key == 'r') {
    record = true;
  }
}
