#version 330 core

in vec2 vScreenPos;
in vec2 vLocalPos;
flat in vec2 vSize;

#define MAX_STOPS 5
uniform int uStopCount;
uniform vec3 uStopColors[MAX_STOPS];
uniform float uStopPositions[MAX_STOPS];

out vec4 FragColor;

vec3 sampleGradient(float x) {
    if (uStopCount <= 0) return vec3(0.0);
    if (uStopCount == 1) return uStopColors[0];

    if (x <= uStopPositions[0]) return uStopColors[0];

    for (int i = 0; i < MAX_STOPS - 1; i++) {
        if (i >= uStopCount - 1) break;

        float a = uStopPositions[i];
        float b = uStopPositions[i + 1];

        if (x >= a && x <= b) {
            float t = (x - a) / max(b - a, 0.00001);
            return mix(uStopColors[i], uStopColors[i + 1], t);
        }
    }

    return uStopColors[uStopCount - 1];
}

void main() {
    vec2 local = clamp(vLocalPos + 0.5, 0.0, 1.0);

    vec3 color = sampleGradient(local.x);
    FragColor = vec4(color, 1.0);
}
