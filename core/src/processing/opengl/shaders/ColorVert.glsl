#version 320 es

uniform mat4 transformMatrix;

in vec4 position;
in vec4 color;

out vec4 vertColor;

void main() {
  gl_Position = transformMatrix * position;
    
  vertColor = color;
}
