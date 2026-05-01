#version 330 core

in vec3 vWorldPos;
in vec3 vNormal;

uniform vec3 uObjectColor;
uniform vec3 uViewPosition;
uniform float uIntensity;

out vec4 FragColor;

void main() {
    vec3 result = uIntensity * uObjectColor;
    FragColor = vec4(result, 1.0);
}
