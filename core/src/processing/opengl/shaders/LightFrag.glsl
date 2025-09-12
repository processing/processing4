#version 320 es
 
#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec4 vertColor;
in vec4 backVertColor;

out colorOut;

void main() {
  colorOut = gl_FrontFacing ? vertColor : backVertColor;
}
