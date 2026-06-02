#version 330 core

layout (location = 0) in vec3 aPos;

out vec2 vUV;

void main() {
    vec2 screenPos = aPos.xy * 2.0;

    vUV = screenPos * 0.5 + 0.5;
    gl_Position = vec4(screenPos, 0.0, 1.0);
}