#version 320 es
#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

uniform vec2 texOffset;

in vec4 vertColor;
in vec4 vertTexCoord;

out vec4 colorOut;

void main() {
  colorOut = texture2D(texture, vertTexCoord.st) * vertColor;
}
