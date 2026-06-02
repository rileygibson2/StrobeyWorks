#version 330 core

layout (location = 0) in vec3 aAgent;

void main() {
    vec2 ndc = aAgent.xy * 2.0 - 1.0;
    gl_Position = vec4(ndc, 0.0, 1.0);
    gl_PointSize = 1.0;
}