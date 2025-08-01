#version 320 es

uniform mat4 transformMatrix;
uniform mat4 texMatrix;

in vec4 position;
in vec4 color;
in vec2 texCoord;

out vec4 vertColor;
out vec4 vertTexCoord;

void main() {
  gl_Position = transformMatrix * position;
    
  vertColor = color;
  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);
}
