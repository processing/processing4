#version 320 es

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

in vec4 vertColor;

out vec4 outColor;

void main() {
  outColor = vertColor;
}
