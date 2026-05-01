#version 330 core

in vec3 vWorldPos;

out vec4 FragColor;

void main() {
    float scale = 1.0;

    float x = floor(vWorldPos.x / scale);
    float z = floor(vWorldPos.z / scale);

    float checker = mod(x + z, 2.0);

    vec3 colorA = vec3(0.08, 0.08, 0.08);
    vec3 colorB = vec3(0.18, 0.18, 0.18);

    vec3 color = mix(colorA, colorB, checker);

    FragColor = vec4(color, 1.0);
}
