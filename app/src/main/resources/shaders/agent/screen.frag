#version 330 core

in vec2 vUV;

uniform sampler2D uDepositTexture;
uniform float uOpacityCuttoff;

out vec4 FragColor;

vec3 colBase = vec3(1.0, 1.0, 1.0);
vec3 colA = vec3(1.0, 1.0, 1.0);
vec3 colB = vec3(0.0, 0.0, 0.2);

void main() {
    float pheromone = texture(uDepositTexture, vUV).r;

    float softness = 0;
    float t;
    if (softness <= 0.0) {
        t = pheromone > uOpacityCuttoff ? 1.0 : 0.0;
    } else {
        t = smoothstep(
            uOpacityCuttoff - softness,
            uOpacityCuttoff + softness,
            pheromone
        );
    }

    if (pheromone <= uOpacityCuttoff) {
        FragColor = vec4(colBase, 0.0);
        return;
    }

    vec3 gradientColor = mix(colA, colB, pheromone);
    vec3 finalColor = mix(colBase, gradientColor, t);

    FragColor = vec4(finalColor, 1.0);
}