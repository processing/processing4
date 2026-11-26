/**
 * Using createGraphics() to Create an SVG File
 * 
 * To write a SVG file using only the createGraphics() command, 
 * rather than as part of a sketch, it's necessary to call 
 * dispose() on the PGraphicsSVG object. This is the same as 
 * calling exit(), but it won't quit the sketch.
 */

import processing.svg.*;

PGraphics svg = createGraphics(300, 300, SVG, "output.svg");
svg.beginDraw();
svg.background(128, 0, 0);
svg.line(50, 50, 250, 250);
svg.dispose();
svg.endDraw();
